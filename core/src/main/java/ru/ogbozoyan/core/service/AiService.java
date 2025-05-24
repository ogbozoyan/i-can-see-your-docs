package ru.ogbozoyan.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    @Autowired
    private ChatClient openAiChatClient;

    @Autowired
    private RestTemplate restTemplate;

    public String imageURI(){
        String imageUrl = "http://176.124.214.22:9001/api/v1/download-shared-object/aHR0cDovLzQ4NjY0MTktY3MxNzAyNDo5MDAwL29jci9pbWcucG5nP1gtQW16LUFsZ29yaXRobT1BV1M0LUhNQUMtU0hBMjU2JlgtQW16LUNyZWRlbnRpYWw9OFdOS0ZZUFVPMFE0NDlVQU1TWUElMkYyMDI1MDUyNCUyRnVzLWVhc3QtMSUyRnMzJTJGYXdzNF9yZXF1ZXN0JlgtQW16LURhdGU9MjAyNTA1MjRUMjA1ODQzWiZYLUFtei1FeHBpcmVzPTQzMjAwJlgtQW16LVNlY3VyaXR5LVRva2VuPWV5SmhiR2NpT2lKSVV6VXhNaUlzSW5SNWNDSTZJa3BYVkNKOS5leUpoWTJObGMzTkxaWGtpT2lJNFYwNUxSbGxRVlU4d1VUUTBPVlZCVFZOWlFTSXNJbVY0Y0NJNk1UYzBPREUxTmpZNU1pd2ljR0Z5Wlc1MElqb2liV2x1YVc5aFpHMXBiaUo5LmgyZ0x1LUo0Z0tfdlQ1cWNRVm9qY2RSWUpQWTNISXR4SGdFZGpxSVlQZVVJc3BzbzI1aUthOE9WMU1vZFBDQmV2b2xBUnMtWkduRllJYXFqaWlFd2VRJlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCZ2ZXJzaW9uSWQ9bnVsbCZYLUFtei1TaWduYXR1cmU9NTM3NjAwMWQxZWUxYWE5ODZjYmJjNWI3NTg0YjIzOGQ3NTI4MTFmYmI1ZjIzZDY4M2YwMTBhNGM3ZjU0NThhZg";
        byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);

        Resource imageResource = new ByteArrayResource(imageBytes);

        return openAiChatClient.prompt()
            .user(userSpec -> userSpec
                .text("")
                .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource))
            )
            .call()
            .content();
    }

}
