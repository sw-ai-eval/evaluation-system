function submitDeptForm(url) {

    const deptId = document.querySelector("select[name='deptId']").value;

    if (url === '/evaluator/create') {
        // 👉 POST form 사용
        document.getElementById("createDeptId").value = deptId;
        document.getElementById("createForm").submit();

    } else {
        // 👉 GET form 사용
        document.getElementById("deptForm").submit();
    }
}
function submitResetForm() {
    const deptId = document.querySelector('select[name="deptId"]').value;

    document.getElementById("resetDeptId").value = deptId;
    document.getElementById("resetForm").submit();
}

document.querySelectorAll('.edit-btn').forEach(btn => {
    btn.addEventListener('click', function () {
        openEditModal(this.dataset.empNo);
    });
});

let allEmployees = [];
let firstEvaluators = [];

console.log(firstEvaluators);

function openEditModal(empNo) {

    const deptNameEl = document.getElementById('deptName');
	const deptIdEl = document.getElementById('deptId');
    const empNoEl = document.getElementById('empNo');
    const empNameEl = document.getElementById('empName');
    const positionEl = document.getElementById('position');
	const finalEl = document.getElementById('finalEvaluatorSelect');
	

    if (!finalEl) {
        console.error("모달 DOM 문제");
        return;
    }

    fetch('/evaluator/detail/' + empNo)
        .then(res => res.json())
		.then(data => {

		    deptNameEl.value = data.deptName ?? '';
		    deptIdEl.value = data.deptId ?? '';
		    empNoEl.value = data.empNo ?? '';
		    empNameEl.value = data.empName ?? '';
		    positionEl.value = data.position ?? '';

		    // ⭐ 1. 먼저 상태 세팅
		    allEmployees = data.availableEmployees ?? [];
		    firstEvaluators = data.firstEvaluators ?? [];

		    // ⭐ 2. 그 다음 렌더
		    renderFirstEvaluators(firstEvaluators);
		    renderAvailableEmployees();

		    // final evaluator
		    setTimeout(() => {
		        finalEl.value = data.finalEvaluator ?? '';
		    }, 0);

		    document.getElementById('editModal').style.display = 'block';
		});
}

function closeModal() {
    document.getElementById('editModal').style.display = 'none';
}

function renderFirstEvaluators(list) {

    const box = document.getElementById("firstEvaluatorsBox");
    const input = document.getElementById("firstEvaluatorsInput");

    box.innerHTML = "";

    list.forEach((emp, index) => {

        const tag = document.createElement("span");
        tag.className = "tag-eval";
        tag.innerHTML = `
            ${emp.name}
            <button type="button" class="tag-eval-button" onclick="removeFirstEvaluator(${emp.empNo})">X</button>
        `;

        box.appendChild(tag);
    });

    input.value = list.map(e => e.empNo).join(",");
}



function removeFirstEvaluator(empNo) {
	firstEvaluators = firstEvaluators.filter(
	        e => e.empNo != empNo
	    );

    renderFirstEvaluators(firstEvaluators);

    renderAvailableEmployees();
}

function addEvaluator() {

    const select = document.getElementById('availableEvaluatorSelect');

    const selectedEmpNo = select.value;

    if (!selectedEmpNo) return;

    // 직원 찾기
    const employee = allEmployees.find(
        emp => emp.empNo == selectedEmpNo
    );

    // 중복 방지
    if (firstEvaluators.some(e => e.empNo == selectedEmpNo)) {
        return;
    }

    firstEvaluators.push({
        empNo: employee.empNo,
        name: employee.name
    });

    renderFirstEvaluators(firstEvaluators);
    renderAvailableEmployees();
}

function renderAvailableEmployees() {

    const select = document.getElementById('availableEvaluatorSelect');

    select.innerHTML = '';
	
	const available = allEmployees.filter(emp =>
	        !firstEvaluators.some(e => e.empNo == emp.empNo)
	    );
		
		
	// ⭐ 데이터 없을 때
    if (!available || available.length === 0) {

        const option = document.createElement('option');
        option.value = '';
        option.textContent = '선택할 사원이 없습니다';
        option.disabled = true;
        option.selected = true;

        select.appendChild(option);
        return;
    }

	available.forEach(emp => {
	       const option = document.createElement('option');
	       option.value = emp.empNo;
	       option.textContent = emp.name;
	       select.appendChild(option);
	   });
}
