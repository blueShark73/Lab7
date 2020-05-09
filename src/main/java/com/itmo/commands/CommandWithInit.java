package com.itmo.commands;

import java.io.IOException;
import java.util.Scanner;

public interface CommandWithInit {
    void init(String argument, Scanner scanner) throws IOException;
}
