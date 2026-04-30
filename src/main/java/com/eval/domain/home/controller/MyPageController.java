package com.eval.domain.home.controller;

import com.eval.domain.employee.dto.EmpManageDTO;
import com.eval.domain.employee.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    // 마이페이지 화면 보여주기
    @GetMapping("/my-page")
    public String myPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            String empNo = userDetails.getUsername(); 
            EmpManageDTO employee = employeeMapper.findByEmpNoDetail(empNo);
            if (employee != null) {
                model.addAttribute("empName", employee.getName());
                model.addAttribute("empNo", employee.getEmpNo());
                model.addAttribute("deptName", employee.getDeptName());
                model.addAttribute("position", employee.getLevelName());
            }
        }
        return "home/my-page"; 
    }

    // 비밀번호 변경 처리 로직
    @PostMapping("/my-page/change-password")
    public String changePassword(@RequestParam("currentPw") String currentPw,
                                 @RequestParam("newPw") String newPw,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes rttr) {
        
        String empNo = userDetails.getUsername();
        EmpManageDTO employee = employeeMapper.findByEmpNoDetail(empNo);

        // 1. 현재 비밀번호가 맞는지 검증
        if (!passwordEncoder.matches(currentPw, employee.getPassword())) {
            // 틀리면 에러 메시지를 담아서 원래 페이지로 돌려보냄
            rttr.addFlashAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
            return "redirect:/my-page"; 
        }

        // 2. 일치하면 새 비밀번호를 암호화해서 DB 업데이트
        String encodedNewPw = passwordEncoder.encode(newPw);
        employeeMapper.updatePassword(empNo, encodedNewPw);

        // 3. 성공 메시지 담아서 돌려보냄
        rttr.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");
        return "redirect:/my-page";
    }
}