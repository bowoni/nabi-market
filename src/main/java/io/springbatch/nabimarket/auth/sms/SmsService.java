package io.springbatch.nabimarket.auth.sms;

public interface SmsService {
    void send(String phoneNumber, String message);
}
