package com.wtl.novel.Controller;


import com.wtl.novel.CDO.LoginRequestCTO;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.InvitationCodeService;
import com.wtl.novel.Service.UserService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.InvitationCode;
import com.wtl.novel.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private InvitationCodeService invitationCodeService;


    @GetMapping("/getPoint")
    public Long getPoint( HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        return userService.getUserPoint(credential.getUser().getId());
    }

//    @GetMapping("/geneCode")
//    public String geneCode( HttpServletRequest httpRequest) {
//        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
//        String authorizationHeader = authorizationInfo[0];
//        Credential credential = credentialService.findByToken(authorizationHeader);
//        return invitationCodeService.createOrGetInvitationCode(credential.getUser().getId()).getCode();
//    }

    @GetMapping("/getCode")
    public String getCode( HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        InvitationCode invitationCode = invitationCodeService.getInvitationCode(credential.getUser().getId());
        return invitationCode == null ? null : invitationCode.getCode();
    }

    @GetMapping("/rewardsPoint/{postId}/{points}")
    public ResponseEntity<String> rewardPoints(
            @PathVariable Long postId,
            @PathVariable Long points,
            HttpServletRequest httpRequest) {
        try {
            String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
            String authorizationHeader = authorizationInfo[0];
            Credential credential = credentialService.findByToken(authorizationHeader);
            // 调用服务层处理打赏逻辑
            boolean success = userService.rewardPoints(postId, credential.getUser().getId(), points);
            if (success) {
                return ResponseEntity.ok("打赏成功");
            } else {
                return ResponseEntity.badRequest().body("打赏失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("打赏失败: " + e.getMessage());
        }
    }
}