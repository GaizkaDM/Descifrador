package com.gaizkaFrost.AES;

public class CryptoException extends RuntimeException {
    CryptoException(String m) { super(m); }
    CryptoException(String m, Throwable c) { super(m, c); }
}

class CryptoFormatException extends RuntimeException {
    CryptoFormatException(String m) { super(m); }
    CryptoFormatException(String m, Throwable c) { super(m, c); }
}

