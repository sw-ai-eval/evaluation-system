document.querySelectorAll('.btn-edit').forEach(button => {
    button.addEventListener('click', async function () {
		// 🔥 추가: 에러 초기화
		const box = document.getElementById("errorBox");
		if (box) {
		    box.style.display = "none";
		    box.textContent = "";
		}
		
		const registerForm = document.querySelector('#registerForm');
		        if (registerForm?.classList.contains('show')) {
		            closeRegisterForm();
		        }
		
		
        const empNo = this.getAttribute('data-emp-no');

        const res = await fetch(`/employee/${empNo}`);
        const emp = await res.json();
		
		console.log(emp);

		const setValue = (selector, value) => {
		    const el = document.querySelector(selector);
		    if (!el) return;

		    // date input이면 포맷 체크
		    if (el.type === "date") {
		        el.value = value ? value.substring(0, 10) : "";
		        return;
		    }

		    el.value = value ?? "";
		};
		const badge = document.querySelector('#employee-update-form #positionBadge');
		
		badge.textContent = emp.position;

		badge.className = emp.position === '부서장'
		    ? 'badge manager'
		    : 'badge staff';
			
		const lockedBadge = document.querySelector('#employee-update-form #lockedBadge');

		if (lockedBadge) {
		    lockedBadge.textContent = emp.locked ? '잠금' : '활성화';
		    lockedBadge.className = emp.locked ? 'badge resigned' : 'badge active';
		}
        setValue('#employee-update-form input[name="empNo"]', emp.empNo);
        setValue('#employee-update-form input[name="name"]', emp.name);
        setValue('#employee-update-form input[name="email"]', emp.email);
        setValue('#employee-update-form input[name="phone"]', emp.phone);
        setValue('#employee-update-form select[name="deptId"]', emp.deptId);
        setValue('#employee-update-form input[name="positionLevel"]', emp.positionLevel);
		setValue('#employee-update-form input[name="job"]', emp.job);
        setValue('#employee-update-form select[name="status"]', emp.status);
		setValue('#employee-update-form input[name="hireDate"]', emp.hireDate);
		setValue('#employee-update-form input[name="resignDate"]', emp.resignDate);
		setValue('#employee-update-form input[name="position"]', emp.position);
		setValue('#employee-update-form select[name="role"]', emp.role);
		
        document.querySelector('#employee-update-form').classList.add('show');
		
		const form = document.querySelector('#employee-update-form form');

        // 중복 이벤트 방지
        form.onsubmit = null;

        form.onsubmit = async function (e) {
            e.preventDefault();

            const formData = new FormData(form);
            const dto = Object.fromEntries(formData.entries());
			
			dto.locked = dto.locked ? 1 : 0;
			dto.hireDate = dto.hireDate || null;
			dto.resignDate = dto.resignDate || null;
			
            const updateRes = await fetch('/employee/update', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(dto)
            });

			if (!updateRes.ok) {
			    const msg = await updateRes.text();

			    const box = document.getElementById("errorBox");
			    box.style.display = "block";
			    box.textContent = msg;

			    return;
			}

            closeEditForm();
            location.reload();
		};
    });
});

function closeEditForm() {
    const form = document.getElementById('employee-update-form');
    form.classList.remove('show');  // 폼을 숨김
}


//////////////////////////////////////////////////////////////////////////////////////// 등록
// 사원 등록 폼을 열기 위한 함수
document.getElementById('open-register-form-btn').addEventListener('click', function() {
	
	if (document.querySelector('#employee-update-form')?.classList.contains('show')) {
	        closeEditForm();
	    }
		
    const form = document.getElementById('employee-register-form');
    form.classList.add('show');  // 폼을 보이게 설정
});
// 등록 폼을 닫는 함수
function closeRegisterForm() {
    const form = document.getElementById('employee-register-form');
    form.classList.remove('show');  // 폼을 숨김
}
// 사원 삭제 처리
function deleteEmployee(empNo) {
    if (confirm("정말 삭제하시겠습니까?")) {
        location.href = '/employee/delete/' + empNo;  // 삭제 처리
    }
}

console.log("사원.js loaded");