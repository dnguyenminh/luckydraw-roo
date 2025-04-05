package vn.com.fecredit.app.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.entity.BlacklistedToken;
import vn.com.fecredit.app.entity.enums.CommonStatus;
import vn.com.fecredit.app.repository.BlacklistedTokenRepository;
import vn.com.fecredit.app.service.BlacklistedTokenService;
import vn.com.fecredit.app.service.base.AbstractServiceImpl;

@Slf4j
@Service
@Transactional
public class BlacklistedTokenServiceImpl extends AbstractServiceImpl<BlacklistedToken> implements BlacklistedTokenService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public BlacklistedTokenServiceImpl(BlacklistedTokenRepository blacklistedTokenRepository) {
        super(blacklistedTokenRepository);
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlacklistedToken> findByToken(String token) {
        return blacklistedTokenRepository.findByToken(token);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlacklistedToken> findByTokenType(String tokenType) {
        return blacklistedTokenRepository.findByTokenType(tokenType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlacklistedToken> findByUserId(Long userId) {
        return blacklistedTokenRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BlacklistedToken> findValidToken(String token, String tokenType, LocalDateTime currentTime) {
        return blacklistedTokenRepository.findValidToken(token, tokenType, currentTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlacklistedToken> findAllValidTokens(LocalDateTime currentTime) {
        return blacklistedTokenRepository.findAllValidTokens(currentTime);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenBlacklisted(String token, String tokenType, LocalDateTime currentTime) {
        return blacklistedTokenRepository.isTokenBlacklisted(token, tokenType, currentTime);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens(LocalDateTime expirationTime) {
        blacklistedTokenRepository.deleteExpiredTokens(expirationTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlacklistedToken> findByStatus(CommonStatus status) {
        return blacklistedTokenRepository.findByStatus(status);
    }
}
