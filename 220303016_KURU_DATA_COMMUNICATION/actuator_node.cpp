#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <string>
#include <chrono>
#include <thread>

static void Die(const char* msg) {
    std::cout << msg << " | WSAGetLastError=" << WSAGetLastError() << "\n";
    exit(1);
}

static std::string GetField(const std::string& msg, const std::string& key) {  //yine value kısmı
    size_t p = msg.find(key);
    if (p == std::string::npos) return "";
    p += key.size();
    size_t e = msg.find('|', p);
    if (e == std::string::npos) e = msg.size();
    return msg.substr(p, e - p);
}

int main() {
    WSADATA wsaData;  //yine Winsock
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) Die("WSAStartup failed");

    SOCKET s = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);  //TCP soketi
    if (s == INVALID_SOCKET) Die("socket failed");

    sockaddr_in gw{};  //gateway adresi 8100, gateway actuator dinliyor
    gw.sin_family = AF_INET;
    gw.sin_port = htons(8100);
    gw.sin_addr.s_addr = inet_addr("127.0.0.1");

    std::cout << "[actuator] Connecting to gateway 127.0.0.1:8100...\n";  //gateway e TCP bağlantısı
    if (connect(s, (sockaddr*)&gw, sizeof(gw)) == SOCKET_ERROR) {
        closesocket(s);
        Die("connect failed (gateway 8100 dinliyor mu?)");
    }
    std::cout << "[actuator] Connected!\n";

    char buf[4096];
    while (true) {  //gatewayden mesaj bekle
        int n = recv(s, buf, sizeof(buf) - 1, 0);  //gelen byte ları string yapmak için 
        if (n > 0) {
            buf[n] = '\0';
            std::string msg = buf;

            std::string type = GetField(msg, "TYPE=");  //yine mesajdan type ve payload ayıkla
            std::string payload = GetField(msg, "PAYLOAD=");

            std::cout << "[actuator] Received: " << msg << "\n";

            if (type == "COMMAND") {  //eğer command ise uygula 
                //komut uygulama simülasyonu
                std::cout << "[actuator] APPLYING: " << payload << "\n";
                std::this_thread::sleep_for(std::chrono::milliseconds(500));

                std::string ack =  //komut uygulandı bilgisi gönderilir
                    "MSGID=0|SENDER=actuator_node|TYPE=ACK|TS=now|PAYLOAD=" + payload + "_APPLIED";

                send(s, ack.c_str(), (int)ack.size(), 0);  //ack mesajını gateway e gönder
                std::cout << "[actuator] Sent ACK: " << ack << "\n";
            }
        }
        else {
            std::cout << "[actuator] Disconnected.\n";
            break;
        }
    }

    closesocket(s);
    WSACleanup();
    return 0;
}
