package io.pivotal.pcfredis.multiredis.tokens;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/token")
public class TokenController {
    private final Tokens tokens;

    @Autowired
    public TokenController(Tokens tokens) {
        this.tokens = tokens;
    }

    @RequestMapping(method= RequestMethod.GET)
    public @ResponseBody Token getToken(@RequestParam(value="id") String id) {
        long startTime = System.currentTimeMillis();
        Token token = tokens.find(id);
        long timeTaken = System.currentTimeMillis() - startTime;
        token.setDuration(timeTaken);
        return token;
    }
}
