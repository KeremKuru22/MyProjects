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

static double RandRange(double a, double b) {  //random double, koordinatları küçük miktarda değiştirmek için
    return a + (b - a) * (double(rand()) / double(RAND_MAX));
}

int main() {
    srand((unsigned)time(nullptr) + 1);

    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) Die("WSAStartup failed");

    SOCKET s = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (s == INVALID_SOCKET) Die("socket failed");

    sockaddr_in gw{};
    gw.sin_family = AF_INET;
    gw.sin_port = htons(8000);
    gw.sin_addr.s_addr = inet_addr("127.0.0.1");

    std::cout << "[sensor_gps] Connecting to gateway 127.0.0.1:8000...\n";
    if (connect(s, (sockaddr*)&gw, sizeof(gw)) == SOCKET_ERROR) {
        closesocket(s);
        Die("connect failed");
    }
    std::cout << "[sensor_gps] Connected!\n";

    //Başlangıç
    double lat = 41.015;
    double lon = 28.979;

    int msgId = 1;
    while (true) {  //çok küçük aralıkta konum değiştirme
        lat += RandRange(-0.0005, 0.0005);
        lon += RandRange(-0.0005, 0.0005);
        int speed = rand() % 6; // 0-5

        std::string payload =  //cloud tarafında herhangi bir kullanım alanı yok, örnek olsun diye koydum, token bucket ın da çalıştığını göstermek için ekstra sensör olsun diye
            "lat=" + std::to_string(lat) +
            ",lon=" + std::to_string(lon) +
            ",speed=" + std::to_string(speed);

        std::string msg =
            "MSGID=" + std::to_string(msgId++) +
            "|SENDER=sensor_gps|TYPE=GPS|TS=now|PAYLOAD=" + payload;

        int sent = send(s, msg.c_str(), (int)msg.size(), 0);
        if (sent == SOCKET_ERROR) {
            std::cout << "[sensor_gps] send error\n";
            break;
        }

        std::cout << "[sensor_gps] Sent: " << msg << "\n";
        std::this_thread::sleep_for(std::chrono::seconds(10));
    }

    closesocket(s);
    WSACleanup();
    return 0;
}
