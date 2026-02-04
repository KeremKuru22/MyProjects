#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <string>
#include <chrono>
#include <thread>
#include <cstdlib>
#include <ctime>

static void Die(const char* msg) {
    std::cout << msg << " | WSAGetLastError=" << WSAGetLastError() << "\n";
    exit(1);
}

int main() {
    srand((unsigned)time(nullptr));  //random üretim gerçekçi olsun diye

    WSADATA wsaData;  //Winsock
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) Die("WSAStartup failed");

    SOCKET s = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);  //TCP
    if (s == INVALID_SOCKET) Die("socket failed");

    sockaddr_in gw{};  //sensörler 8000
    gw.sin_family = AF_INET;
    gw.sin_port = htons(8000);
    gw.sin_addr.s_addr = inet_addr("127.0.0.1");

    std::cout << "[sensor_weather] Connecting to gateway 127.0.0.1:8000...\n";  //Bağlantı
    if (connect(s, (sockaddr*)&gw, sizeof(gw)) == SOCKET_ERROR) {
        closesocket(s);
        Die("connect failed");
    }
    std::cout << "[sensor_weather] Connected!\n";

    int msgId = 1;
    while (true) {
        int tempC = 10 + rand() % 26;        // 10-35
        int humidity = 30 + rand() % 61;     // 30-90
        int rainProb = rand() % 101;         // 0-100

        std::string payload = 
            "tempC=" + std::to_string(tempC) +
            ",humidity=" + std::to_string(humidity) +
            ",rainProb=" + std::to_string(rainProb);  //cloud tarafı karar verir(IRRIGATE/DELAY)

        std::string msg =
            "MSGID=" + std::to_string(msgId++) +
            "|SENDER=sensor_weather|TYPE=WEATHER|TS=now|PAYLOAD=" + payload;

        int sent = send(s, msg.c_str(), (int)msg.size(), 0);
        if (sent == SOCKET_ERROR) {
            std::cout << "[sensor_weather] send error\n";
            break;
        }

        std::cout << "[sensor_weather] Sent: " << msg << "\n";  //konsol
        std::this_thread::sleep_for(std::chrono::seconds(8));
    }

    closesocket(s);
    WSACleanup();
    return 0;
}
