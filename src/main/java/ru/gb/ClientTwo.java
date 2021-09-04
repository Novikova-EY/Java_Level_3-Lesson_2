package ru.gb;

import ru.gb.client.ClientChatAdapter;

public class ClientTwo {
    public static void main(String[] args) {
        new ClientChatAdapter("localhost", 8989);
    }
}
