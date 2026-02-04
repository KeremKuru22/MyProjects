#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <winsock2.h>
#include <ws2tcpip.h>
#undef min  //std:min ile çakışıyordu
#undef max
#include <algorithm>
#include <iostream>
#include <string>
#include <thread>
#include <mutex>
#include <queue>
#include <atomic>

// TOKEN BUCKET icin
#include <unordered_map>
#include <chrono>
#include <algorithm>

static void Die(const char* msg) {  //Hata alma durumunda durdurmak için
    std::cout << msg << " | WSAGetLastError=" << WSAGetLastError() << "\n";
    exit(1);
}

static std::string GetField(const std::string& msg, const std::string& key) {  //key'in value kısmını seç
    size_t p = msg.find(key);
    if (p == std::string::npos) return "";
    p += key.size();
    size_t e = msg.find('|', p);
    if (e == std::string::npos) e = msg.size();
    return msg.substr(p, e - p);
}

//TokenBucket sensör trafiği sınırı
struct TokenBucket {
    double tokens;         //mevcut olan
    double rate_per_sec;   //saniyede token sayısı
    double capacity;       //max token
    std::chrono::steady_clock::time_point last;

    TokenBucket() = default;  //Kapasite kadar başlar
    TokenBucket(double rate, double cap)
        : tokens(cap), rate_per_sec(rate), capacity(cap), last(std::chrono::steady_clock::now()) {
    }

    bool allow_one() {  //Mesaj geçebilir mi kontrolü
        auto now = std::chrono::steady_clock::now();
        std::chrono::duration<double> dt = now - last;
        last = now;

        tokens = std::min(capacity, tokens + dt.count() * rate_per_sec);  //Geçen zamana göre 

        if (tokens >= 1.0) {  //En az 1 varsa geçer
            tokens -= 1.0;
            return true;
        }
        return false;
    }
};
//Her sensör için token bucket
std::mutex g_tbMtx;
std::unordered_map<std::string, TokenBucket> g_buckets;


//Ayarlar 1 mesaj / 5 saniye => 0.2 token/sn
static const double TB_RATE_PER_SEC = 0.2;
// burst (anlik) izni: 3 mesaj
static const double TB_BURST = 3.0;

static bool AllowByTokenBucket(const std::string& sender) {  //sensör için token bucket kontrolü
    std::lock_guard<std::mutex> lk(g_tbMtx);
    auto it = g_buckets.find(sender);
    if (it == g_buckets.end()) {
        it = g_buckets.emplace(sender, TokenBucket(TB_RATE_PER_SEC, TB_BURST)).first;
    }
    return it->second.allow_one();
}

//cloud a gidecek mesajlar kuyruğu(buffer)
std::mutex g_mtx;
std::queue<std::string> g_toCloudQueue;
//actuator soketi
std::mutex g_actMtx;
SOCKET g_actuatorSock = INVALID_SOCKET;

std::atomic<bool> g_running(true);  //gateway çalışma durumu
SOCKET g_cloudSock = INVALID_SOCKET;  //cloud bağlantı soketi

//sensörler (8000)
void HandleSensorClient(SOCKET s) {
    char buf[4096];
    while (g_running) {
        int n = recv(s, buf, sizeof(buf) - 1, 0);
        if (n > 0) {
            buf[n] = '\0';
            std::string msg = buf;

            //hangi sensör olduğunu bul
            std::string sender = GetField(msg, "SENDER=");
            if (sender.empty()) sender = "unknown";
            //token bucket ile limit
            if (!AllowByTokenBucket(sender)) {
                std::cout << "[gateway] DROP (token_bucket) sender=" << sender
                    << " msg=" << msg << "\n";
                continue;
            }
            //cloud kuyruğuna ekleme
            {
                std::lock_guard<std::mutex> lk(g_mtx);
                g_toCloudQueue.push(msg);
            }
            std::cout << "[gateway] PASS (token_bucket) sender=" << sender
                << " msg=" << msg << "\n";
        }
        else {
            break;
        }
    }
    closesocket(s);
    std::cout << "[gateway] Sensor thread ended.\n";
}

void SensorAcceptThread() {  //sensör bağlantılarını kabul eden thread
    SOCKET listenSock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (listenSock == INVALID_SOCKET) Die("[gateway] sensor listener socket failed");

    sockaddr_in addr{};
    addr.sin_family = AF_INET;
    addr.sin_port = htons(8000);
    addr.sin_addr.s_addr = INADDR_ANY;

    if (bind(listenSock, (sockaddr*)&addr, sizeof(addr)) == SOCKET_ERROR) {
        closesocket(listenSock);
        Die("[gateway] bind failed (8000 portu dolu olabilir)");
    }
    if (listen(listenSock, SOMAXCONN) == SOCKET_ERROR) {
        closesocket(listenSock);
        Die("[gateway] listen failed");
    }

    std::cout << "[gateway] Listening sensors on port 8000...\n";

    while (g_running) {
        sockaddr_in ca{};
        int clen = sizeof(ca);
        SOCKET s = accept(listenSock, (sockaddr*)&ca, &clen);
        if (s == INVALID_SOCKET) continue;

        std::cout << "[gateway] Sensor connected.\n";
        std::thread t(HandleSensorClient, s);
        t.detach();
    }

    closesocket(listenSock);
}

//actuator(8100)
void HandleActuatorClient(SOCKET s) {  //gelen mesajları işle
    {
        std::lock_guard<std::mutex> lk(g_actMtx);
        g_actuatorSock = s;
    }
    std::cout << "[gateway] Actuator connected.\n";

    char buf[4096];
    while (g_running) {
        int n = recv(s, buf, sizeof(buf) - 1, 0);
        if (n > 0) {
            buf[n] = '\0';
            std::string msg = buf;
            std::cout << "[gateway] From actuator: " << msg << "\n";

            //ACK mesajları cloud a gönder
            {
                std::lock_guard<std::mutex> lk(g_mtx);
                g_toCloudQueue.push(msg);
            }
        }
        else {
            break;
        }
    }

    closesocket(s);
    {
        std::lock_guard<std::mutex> lk(g_actMtx);
        g_actuatorSock = INVALID_SOCKET;
    }
    std::cout << "[gateway] Actuator disconnected.\n";
}

void ActuatorAcceptThread() {  //actuator bağlantılarını kabul et
    SOCKET listenSock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (listenSock == INVALID_SOCKET) Die("[gateway] actuator listener socket failed");

    sockaddr_in addr{};
    addr.sin_family = AF_INET;
    addr.sin_port = htons(8100);
    addr.sin_addr.s_addr = INADDR_ANY;

    if (bind(listenSock, (sockaddr*)&addr, sizeof(addr)) == SOCKET_ERROR) {
        closesocket(listenSock);
        Die("[gateway] bind failed (8100 portu dolu olabilir)");
    }
    if (listen(listenSock, SOMAXCONN) == SOCKET_ERROR) {
        closesocket(listenSock);
        Die("[gateway] listen failed (8100)");
    }

    std::cout << "[gateway] Listening actuator on port 8100...\n";

    while (g_running) {
        sockaddr_in ca{};
        int clen = sizeof(ca);
        SOCKET s = accept(listenSock, (sockaddr*)&ca, &clen);
        if (s == INVALID_SOCKET) continue;

        //Tek actuator mantığı: yenisi gelirse eskisini kapat
        {
            std::lock_guard<std::mutex> lk(g_actMtx);
            if (g_actuatorSock != INVALID_SOCKET) {
                closesocket(g_actuatorSock);
                g_actuatorSock = INVALID_SOCKET;
            }
        }

        std::thread t(HandleActuatorClient, s);
        t.detach();
    }

    closesocket(listenSock);
}

//cloud(9000)
void CloudConnect() {  //gateway cloud a bağlanır (client gibi davranır)
    g_cloudSock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (g_cloudSock == INVALID_SOCKET) Die("[gateway] cloud socket failed");

    sockaddr_in server{};
    server.sin_family = AF_INET;
    server.sin_port = htons(9000);
    server.sin_addr.s_addr = inet_addr("127.0.0.1");

    std::cout << "[gateway] Connecting to cloud_server 127.0.0.1:9000...\n";
    if (connect(g_cloudSock, (sockaddr*)&server, sizeof(server)) == SOCKET_ERROR) {
        closesocket(g_cloudSock);
        Die("[gateway] connect() failed");
    }
    std::cout << "[gateway] Connected to cloud!\n";
}
//kuyruktaki mesajları cloud a gönder
void CloudSenderThread() {
    while (g_running) {
        std::string msg;
        {
            std::lock_guard<std::mutex> lk(g_mtx);
            if (!g_toCloudQueue.empty()) {
                msg = g_toCloudQueue.front();
                g_toCloudQueue.pop();
            }
        }

        if (!msg.empty()) {
            int sent = send(g_cloudSock, msg.c_str(), (int)msg.size(), 0);
            if (sent == SOCKET_ERROR) {
                std::cout << "[gateway] send to cloud error | " << WSAGetLastError() << "\n";
                break;
            }
            std::cout << "[gateway] Forwarded to cloud: " << msg << "\n";
        }
        else {
            Sleep(30);
        }
    }
}
//cloud'dan gelen COMMAND mesajları actuator a gider
void CloudReceiverThread() {
    char buf[4096];
    while (g_running) {
        int n = recv(g_cloudSock, buf, sizeof(buf) - 1, 0);
        if (n > 0) {
            buf[n] = '\0';
            std::string msg = buf;
            std::cout << "[gateway] From cloud: " << msg << "\n";

            std::string type = GetField(msg, "TYPE=");
            std::string payload = GetField(msg, "PAYLOAD=");

            if (type == "COMMAND") {
                
                SOCKET actSock;
                {
                    std::lock_guard<std::mutex> lk(g_actMtx);
                    actSock = g_actuatorSock;
                }

                if (actSock == INVALID_SOCKET) {
                    std::cout << "[gateway] WARNING: Actuator not connected, cannot deliver COMMAND.\n";
                    continue;
                }

                int sent = send(actSock, msg.c_str(), (int)msg.size(), 0);
                if (sent == SOCKET_ERROR) {
                    std::cout << "[gateway] send to actuator error | " << WSAGetLastError() << "\n";
                }
                else {
                    std::cout << "[gateway] >>> Delivered COMMAND to actuator: " << payload << "\n";
                }
            }
        }
        else {
            std::cout << "[gateway] Cloud disconnected.\n";
            break;
        }
    }
}

int main() {
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) Die("WSAStartup failed");

    CloudConnect();

    std::thread tSensors(SensorAcceptThread);
    std::thread tAct(ActuatorAcceptThread);
    std::thread tCloudSend(CloudSenderThread);
    std::thread tCloudRecv(CloudReceiverThread);

    tSensors.join();
    tAct.join();
    tCloudSend.join();
    tCloudRecv.join();

    if (g_cloudSock != INVALID_SOCKET) closesocket(g_cloudSock);
    WSACleanup();
    return 0;
}
