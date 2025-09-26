package com.miracle.coordifit.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 회원가입 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password") // 비밀번호는 로그에서 제외
public class SignUpRequestDto {

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	private String email;

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
	private String password;

	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다.")
	private String nickname;

	@NotBlank(message = "이메일 인증 코드는 필수입니다.")
	private String verificationCode;
}
