package io.springbatch.nabimarket.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!prod")
public class MockSmsService implements SmsService {

    @Override
    public void send(String phoneNumber, String message) {
        log.info("[MOCK SMS] To: {} | Message: {}", phoneNumber, message);
    }

}
