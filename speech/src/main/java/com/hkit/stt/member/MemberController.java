package com.hkit.stt.member;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hkit.stt.trans.TutorialService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

	private final MemberService ms;
	private final TutorialService tutorialService;
	 private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
	
	@PostMapping("/checkId")
	@ResponseBody
	public String checkId(@RequestParam String id) {
		logger.debug("Received request to check ID: {}", id);
        boolean isAvailable = ms.isIdAvailable(id);
        logger.debug("ID {} is available: {}", id, isAvailable);
        return isAvailable ? "available" : "duplicate";
		}

	@GetMapping("/modify")
	public String ViewModifyForm(Model model, Principal pc) {
		String memberId = pc.getName();
		Member member = ms.getMember(ms.getMemberNum(memberId));
		model.addAttribute("memberForm", member);
		return "modify_form";
	}

	@PostMapping("/modify")
	public String ProModifyForm(@ModelAttribute("memberForm") Member memberForm, @RequestParam("id") String id,
			@RequestParam("password") String password, @RequestParam("name") String name,
			@RequestParam("email") String email, @RequestParam("phoneNumber") String phoneNumber) {
		ms.modify(memberForm, id, password, name, email, phoneNumber);
		return "redirect:/members/mypage";
	}
	
	//마이페이지 그래프
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/mypage")
	public String mypage(
	        Model model,
	        Principal principal,
	        @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
	        @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
	    
	    if (startDate == null) {
	        startDate = LocalDateTime.of(1999, 1, 1, 0, 0, 0);
	    }
	    if (endDate == null) {
	        endDate = LocalDateTime.now();
	    }
	    
	    String memberId = principal.getName();
	    Member member = ms.getMember(ms.getMemberNum(memberId));
	    
	    // 회원 정보 설정
	    model.addAttribute("MemberId", member.getId());
	    model.addAttribute("MemberName", member.getName());
	    model.addAttribute("MemberEmail", member.getEmail());
	    model.addAttribute("MemberPhone", member.getPhoneNumber());
	    model.addAttribute("apiKey", member.getApiKey());
	    
	    // 월별 사용량 데이터 가져오기
	    Map<YearMonth, Map<String, Double>> monthlyUsage = tutorialService.getMonthlyUsageForMember(member.getMemberNum(), startDate, endDate);
	    model.addAttribute("monthlyUsage", monthlyUsage);
	    
	    // Open API 총 사용 시간 계산
	    double totalOpenApiUsage = monthlyUsage.values().stream()
	            .mapToDouble(m -> m.getOrDefault("Open API 사용", 0.0))
	            .sum();
	    
	    // 시간으로 변환하고 소수점 둘째 자리까지 반올림
	    String formattedTotalOpenApiUsage = String.format("%.2f", totalOpenApiUsage / 60);
	    model.addAttribute("totalOpenApiUsage", formattedTotalOpenApiUsage);
	    return "mypage";
	}

	@GetMapping("/login")
	public String login() {
		return "login_form";
	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		new SecurityContextLogoutHandler().logout(request, response,
				SecurityContextHolder.getContext().getAuthentication());
		return "redirect:/";
	}

	@GetMapping("/signup")
	public String create(MemberForm memberForm) {
		return "sign_form";
	}

	@PostMapping("/signup")
	public String create(@Valid MemberForm mf, BindingResult br, Model model) {

		if (br.hasErrors()) {
			return "sign_form";
		}

		if (!mf.getPassword().equals(mf.getPassword2())) {
			br.rejectValue("password2", "passwordInCorrect", "비밀번호가 일치하지 않습니다.");
			return "sign_form";
		}

		try {
			ms.create(mf.getId(), mf.getPassword(), mf.getName(), mf.getEmail(), mf.getPhoneNumber());

		} catch (RuntimeException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "sign_form";
		}

		return "redirect:/";
	}

	// api키발급
	@PostMapping("/generate-api-key")
	public String generateApiKey(Principal principal, Model model, RedirectAttributes redirectAttributes) {
		String id = principal.getName();
		log.info(id);

		
		 String apiKey = ms.generateApiKey(id);
		 redirectAttributes.addFlashAttribute("apiKey", apiKey);
		
		return "redirect:/members/mypage";
	}

	@GetMapping("/api-key")
	@ResponseBody
	public String getApiKey(Principal principal) {
		if (principal != null) {
			String id = principal.getName();
			String apiKey = ms.getApiKey(id);
			if (apiKey != null) {
				return apiKey;
			} else {
				return "API 키가 아직 생성되지 않았습니다.";
			}
		}
		return "로그인이 필요합니다.";
	}
	
    @GetMapping
    public String getAllMembers(Model model) {
        List<Member> members = ms.getAllMembers();
        model.addAttribute("members", members);
        return "members";
    }
	
	@PostMapping
	public Member addMember(@RequestBody Member member) {
		return ms.addMember(member);
	}
}
