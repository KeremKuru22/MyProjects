#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <winsock2.h>
#include <ws2tcpip.h>
#include <iostream>
#include <string>
#include <chrono>
#include <thread>
#include <cstdlib>
#include <ctime>
                                               //gps gibi cloud da herhangi bir işlemi, kullanım alanı yok
static void Die(const char* msg) {
    std::cout << msg << " | WSAGetLastError=" << WSAGetLastError() << "\n";
    exit(1);
}

int main() {
    srand((unsigned)time(nullptr) + 2);

    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) Die("WSAStartup failed");

    SOCKET s = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (s == INVALID_SOCKET) Die("socket failed");

    sockaddr_in gw{};
    gw.sin_family = AF_INET;
    gw.sin_port = htons(8000);
    gw.sin_addr.s_addr = inet_addr("127.0.0.1");

    std::cout << "[sensor_thermal] Connecting to gateway 127.0.0.1:8000...\n";
    if (connect(s, (sockaddr*)&gw, sizeof(gw)) == SOCKET_ERROR) {
        closesocket(s);
        Die("connect failed");
    }
    std::cout << "[sensor_thermal] Connected!\n";

    int msgId = 1;
    while (true) {
        int surfaceTemp = 15 + rand() % 26; // 15-40

        std::string msg =
            "MSGID=" + std::to_string(msgId++) +
            "|SENDER=sensor_thermal|TYPE=THERMAL|TS=now|PAYLOAD=" +
            std::to_string(surfaceTemp);

        int sent = send(s, msg.c_str(), (int)msg.size(), 0);
        if (sent == SOCKET_ERROR) {
            std::cout << "[sensor_thermal] send error\n";
            break;
        }

        std::cout << "[sensor_thermal] Sent: " << msg << "\n";
        std::this_thread::sleep_for(std::chrono::seconds(9));
    }

    closesocket(s);
    WSACleanup();
    return 0;
}
