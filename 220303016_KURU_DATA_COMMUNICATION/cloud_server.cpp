#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <winsock2.h>  //Windows soket API 
#include <ws2tcpip.h>
#include <iostream>
#include <thread>
#include <string>
#include <mutex>
#include <optional>

static void Die(const char* msg) {
    std::cout << msg << " | WSAGetLastError=" << WSAGetLastError() << "\n";  //Hatada dur
    exit(1);
}

static std::string GetField(const std::string& msg, const std::string& key) {
    // key örneği TYPE=MOISTURE  value değeri 
    size_t p = msg.find(key);
    if (p == std::string::npos) return "";
    p += key.size();
    size_t e = msg.find('|', p);
    if (e == std::string::npos) e = msg.size();
    return msg.substr(p, e - p);
}

static std::optional<int> ParseIntPayload(const std::string& payload) {      //stoi ile int e çevir
    try { return std::stoi(payload); }
    catch (...) { return std::nullopt; }
}

static std::optional<int> ParseRainProb(const std::string& payload) {
    //WEATHER sensöründen gelen PAYLOAD
    //rainProb int çevir
    size_t p = payload.find("rainProb=");
    if (p == std::string::npos) return std::nullopt;
    p += std::string("rainProb=").size();
    size_t e = payload.find(',', p);
    std::string v = (e == std::string::npos) ? payload.substr(p) : payload.substr(p, e - p);
    try { return std::stoi(v); }
    catch (...) { return std::nullopt; }
}

void HandleClient(SOCKET clientSock, sockaddr_in clientAddr) {  //cloud server a bağlanan client(gateway) için 
    char clientIp[INET_ADDRSTRLEN]{}; //IP=okunabilir string
    inet_ntop(AF_INET, &clientAddr.sin_addr, clientIp, sizeof(clientIp));
    std::cout << "[cloud_server] Client connected: " << clientIp << ":" << ntohs(clientAddr.sin_port) << "\n";

    int lastMoisture = -1; //-1 veri gelmedi
    int lastRainProb = -1;
    std::string lastCommand = ""; //aynısı gönderme sonuncuyu sakla

    auto EvaluateAndCommand = [&]() { //karar verme 
        if (lastMoisture < 0 || lastRainProb < 0) return; //iki veri de gelmeli

        std::string cmd = (lastMoisture < 30 && lastRainProb < 30) ? "IRRIGATE" : "DELAY"; //koşul sağlanırsa sulama aç
        if (cmd == lastCommand) return; //aynısını spam yapma

        std::string out =
            "MSGID=0|SENDER=cloud_server|TYPE=COMMAND|TS=now|PAYLOAD=" + cmd; //gateway e gidecek komut mesajı

        int sent = send(clientSock, out.c_str(), (int)out.size(), 0);  //TCP üzerinden clientSock(gateway) e gönder
        if (sent == SOCKET_ERROR) {
            std::cout << "[cloud_server] send COMMAND error | " << WSAGetLastError() << "\n";
            return;
        }
        lastCommand = cmd;
        std::cout << "[cloud_server] >>> SENT COMMAND: " << out << "\n";
        };

    char buf[4096];  //client'tan veri almak için buffer
    while (true) {  //gateway açık olduğu sürece veri alır
        int n = recv(clientSock, buf, sizeof(buf) - 1, 0);
        if (n > 0) {
            buf[n] = '\0';
            std::string msg = buf;

            std::string type = GetField(msg, "TYPE="); //mesajın içinden çıkar
            std::string payload = GetField(msg, "PAYLOAD=");

            std::cout << "[cloud_server] Received: " << msg << "\n";

            if (type == "MOISTURE") {  //MOISTURE geldiğinden payload sayıya çevir
                auto v = ParseIntPayload(payload);
                if (v) {
                    lastMoisture = *v;
                    std::cout << "[cloud_server] lastMoisture=" << lastMoisture << "\n";
                    EvaluateAndCommand();  //yeni veri geldi kontrol
                }
            }
            else if (type == "WEATHER") {  //WEATHER geldiğinde rainProb 
                auto rp = ParseRainProb(payload);
                if (rp) {
                    lastRainProb = *rp;
                    std::cout << "[cloud_server] lastRainProb=" << lastRainProb << "\n";
                    EvaluateAndCommand();
                }
            }
            else if (type == "ACK") {  //Actuator geri dönüş mesajı logla
                std::cout << "[cloud_server] <<< ACK from actuator: " << payload << "\n";
            }
        }
        else if (n == 0) {  //Bağlantı kapandı
            std::cout << "[cloud_server] Client disconnected.\n";
            break;
        }
        else {  //Hata
            std::cout << "[cloud_server] recv() error | " << WSAGetLastError() << "\n";
            break;
        }
    }

    closesocket(clientSock);
}

int main() {  //server başlangıcı
    WSADATA wsaData;  //Winsock başlat
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) Die("WSAStartup failed");

    SOCKET listenSock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);  //TCP socket oluştur
    if (listenSock == INVALID_SOCKET) Die("socket() failed");

    sockaddr_in addr{};  //server adres ayarları
    addr.sin_family = AF_INET;
    addr.sin_port = htons(9000);
    addr.sin_addr.s_addr = INADDR_ANY;

    if (bind(listenSock, (sockaddr*)&addr, sizeof(addr)) == SOCKET_ERROR) {  //9000 e 
        closesocket(listenSock);
        Die("bind() failed (Port 9000 dolu olabilir)");
    }

    if (listen(listenSock, SOMAXCONN) == SOCKET_ERROR) {  //gelen bağlantıları dinle
        closesocket(listenSock);
        Die("listen() failed");
    }

    std::cout << "[cloud_server] Listening on port 9000...\n";

    while (true) {  //accept döngüsü, yeni client için thread
        sockaddr_in clientAddr{};
        int clientLen = sizeof(clientAddr);
        SOCKET clientSock = accept(listenSock, (sockaddr*)&clientAddr, &clientLen);
        if (clientSock == INVALID_SOCKET) continue;

        std::thread t(HandleClient, clientSock, clientAddr);  //her client ayrı thread de işlenir
        t.detach();
    }
}
