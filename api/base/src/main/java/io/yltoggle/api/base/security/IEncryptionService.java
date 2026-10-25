package io.yltoggle.api.base.security;

public interface IEncryptionService {

    String encrypt(String content);

    String decrypt(String encryptContent);

}
