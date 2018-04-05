package io.pivotal.pcfredis.multiredis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/token")
public class TokenController {
    private final TokenRepository tokenRepository;

    public TokenController(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @RequestMapping(method= RequestMethod.GET)
    public @ResponseBody Token getToken(@RequestParam(value="id") String id) {
        long startTime = System.currentTimeMillis();
        Token token = tokenRepository.findOnFirstCache(id);
        token.duration = System.currentTimeMillis() - startTime;
        return token;
    }
}
