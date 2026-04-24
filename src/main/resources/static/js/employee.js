document.querySelectorAll('.btn-edit').forEach(button => {
    button.addEventListener('click', async function () {

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

        setValue('#employee-update-form input[name="empNo"]', emp.empNo);
        setValue('#employee-update-form input[name="name"]', emp.name);
        setValue('#employee-update-form input[name="email"]', emp.email);
        setValue('#employee-update-form input[name="phone"]', emp.phone);
        setValue('#employee-update-form select[name="deptId"]', emp.deptId);
        setValue('#employee-update-form input[name="positionLevel"]', emp.positionLevel);
        setValue('#employee-update-form select[name="status"]', emp.status);
		setValue('#employee-update-form input[name="hireDate"]', emp.hireDate);
		setValue('#employee-update-form input[name="resignDate"]', emp.resignDate);

        document.querySelector('#employee-update-form').classList.add('show');
    });
});

function closeEditForm() {
    const form = document.getElementById('employee-update-form');
    form.classList.remove('show');  // 폼을 숨김
}


//////////////////////////////////////////////////////////////////////////////////////// 등록
// 사원 등록 폼을 열기 위한 함수
document.getElementById('open-register-form-btn').addEventListener('click', function() {
    const form = document.getElementById('employee-register-form');
    form.classList.add('show');  // 폼을 보이게 설정
});
// 수정 폼을 닫는 함수
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