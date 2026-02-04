#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <string>
#include <chrono>
#include <thread>
#include <cstdlib>

static void Die(const char* msg) {
    std::cout << msg << " | WSAGetLastError=" << WSAGetLastError() << "\n";
    exit(1);
}

int main() {
    WSADATA wsaData;  //Winsock
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0)
        Die("WSAStartup failed");

    SOCKET s = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);  //TCP oluştur
    if (s == INVALID_SOCKET)
        Die("socket failed");

    sockaddr_in gateway{};  //gateway adresi 8000, sensörler bu port ile bağlanır
    gateway.sin_family = AF_INET;
    gateway.sin_port = htons(8000);
    gateway.sin_addr.s_addr = inet_addr("127.0.0.1");

    std::cout << "[sensor_moisture] Connecting to gateway 127.0.0.1:8000...\n";  //bağlantı
    if (connect(s, (sockaddr*)&gateway, sizeof(gateway)) == SOCKET_ERROR) {
        closesocket(s);
        Die("connect failed");
    }

    std::cout << "[sensor_moisture] Connected!\n";

    int msgId = 1;  //mesaj id leri
    while (true) {  //sürekli veri
        int moisture = 20 + rand() % 40; // 20-60 arası random nem

        std::string msg =  //yine mesaj formatı
            "MSGID=" + std::to_string(msgId++) +
            "|SENDER=sensor_moisture|TYPE=MOISTURE|TS=now|PAYLOAD=" +
            std::to_string(moisture);

        int sent = send(s, msg.c_str(), (int)msg.size(), 0);  //mesaj gönderme
        if (sent == SOCKET_ERROR) {
            std::cout << "[sensor_moisture] send error\n";
            break;
        }

        std::cout << "[sensor_moisture] Sent: " << msg << "\n";  //konsola yazdırma
        std::this_thread::sleep_for(std::chrono::seconds(7));    //7 saniyede bir gönderi
    }

    closesocket(s);
    WSACleanup();
    return 0;
}
