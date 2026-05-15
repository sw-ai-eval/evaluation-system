function submitDeptForm(url) {

    const deptId = document.querySelector("select[name='deptId']").value;

    const typeId = document.querySelector("select[name='typeId']").value;


    if (url === '/evaluator/create') {
        document.getElementById("createDeptId").value = deptId;

        document.getElementById("createTypeId").value = typeId;

        document.getElementById("createForm").submit();

    } else {

        document.getElementById("deptForm").submit();
    }
}
function submitResetForm() {
    const deptId = document.querySelector('select[name="deptId"]').value;
	const typeId = document.querySelector("select[name='typeId']").value;
	
	
	document.getElementById("resetTypeId").value = typeId;

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
	const typeId = document.querySelector("select[name='typeId']").value;
	const evalTypeInput = document.getElementById("evalTypeName");
	const typeIdInput = document.getElementById("modalTypeId");
	

    if (!finalEl) {
        console.error("모달 DOM 문제");
        return;
    }
	

    fetch(`/evaluator/detail/${empNo}?typeId=${typeId}`)
        .then(res => res.json())
		.then(data => {

		    deptNameEl.value = data.deptName ?? '';
		    deptIdEl.value = data.deptId ?? '';
		    empNoEl.value = data.empNo ?? '';
		    empNameEl.value = data.empName ?? '';
		    positionEl.value = data.position ?? '';
			
			typeIdInput.value = typeId;
			typeIdInput.dataset.typeId = typeId; // dataset으로 JS 접근용
			evalTypeInput.value = data.evalTypeName ?? '';
			evalTypeInput.dataset.evalTypeName = data.evalTypeName ?? '';
			
		    // ⭐ 1. 먼저 상태 세팅
		    allEmployees = data.availableEmployees ?? [];
		    firstEvaluators = data.firstEvaluators ?? [];

		    // ⭐ 2. 그 다음 렌더
			renderFirstEvaluators(firstEvaluators, data.evalTypeName);
			renderAvailableEmployees(data.evalTypeName);

		    // final evaluator
		    document.getElementById('finalEvaluatorSelect').value = data.finalEvaluator ?? '';
		    document.getElementById('editModal').style.display = 'block';
		});
}

function closeModal() {
    document.getElementById('editModal').style.display = 'none';
}

function renderFirstEvaluators(list, evalTypeName) {
    const box = document.getElementById("firstEvaluatorsBox");
    const input = document.getElementById("firstEvaluatorsInput");
    const addBtn = document.querySelector(".add-evaluator-btn");
    const select = document.getElementById("availableEvaluatorSelect");

    box.innerHTML = "";

    list.forEach(emp => {
        const tag = document.createElement("span");
        tag.className = "tag-eval";
        tag.innerHTML = `
            ${emp.name}
            <button type="button" class="tag-eval-button" onclick="removeFirstEvaluator('${emp.empNo}', '${evalTypeName}')">X</button>
        `;
        box.appendChild(tag);
    });

    // 숨기기/비활성화 처리
    if (!evalTypeName.includes('다면') && list.length >= 1) {
        select.disabled = true;
        addBtn.disabled = true;
    } else {
        select.disabled = false;
        addBtn.disabled = false;
    }

    input.value = list.map(e => e.empNo).join(",");
}



function removeFirstEvaluator(empNo, evalTypeName) {
    firstEvaluators = firstEvaluators.filter(
        e => e.empNo != empNo
    );

    renderFirstEvaluators(firstEvaluators, evalTypeName);

    renderAvailableEmployees(evalTypeName);
}

function addEvaluator() {

    const select = document.getElementById('availableEvaluatorSelect');

    const selectedEmpNo = select.value;

    if (!selectedEmpNo) return;

    // 직원 찾기
    const employee = allEmployees.find(
        emp => emp.empNo == selectedEmpNo
    );
	
	const evalTypeInput = document.getElementById('evalTypeName');
    const evalTypeName = evalTypeInput.dataset.evalTypeName ?? '';

    // 다면 평가 아닌 경우 1명만 가능
    if (!evalTypeName.includes('다면') && firstEvaluators.length >= 1) {
        alert("이 평가는 1차 평가자가 1명만 가능합니다.");
        return;
    }

    // 중복 방지
    if (firstEvaluators.some(e => e.empNo == selectedEmpNo)) {
        return;
    }

    firstEvaluators.push({
        empNo: employee.empNo,
        name: employee.name
    });

	renderFirstEvaluators(firstEvaluators, evalTypeName);
	renderAvailableEmployees(evalTypeName);
}

function renderAvailableEmployees(evalTypeName) {

    const select = document.getElementById('availableEvaluatorSelect');

    select.innerHTML = '';
	
	const available = allEmployees.filter(emp =>
	        !firstEvaluators.some(e => e.empNo == emp.empNo)
	    );
		
		
	// 다면
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
	   
	   // 1명만 가능이면 select 비활성화 처리
	   const addBtn = document.querySelector('.add-evaluator-btn');

	   // 🔹 다면 평가면 항상 활성, 1명만 가능한 평가는 1명 이상이면 비활성
	   if (!evalTypeName.includes('다면') && firstEvaluators.length >= 1) {
	       select.disabled = true;
	       addBtn.disabled = true;
	   } else {
	       select.disabled = false;
	       addBtn.disabled = false;
	   }
}


function deleteTarget() {

    const empNo = document.getElementById("empNo").value;
    const deptId = document.getElementById("deptId").value;

    // 현재 선택된 typeId 가져오기
    const typeId =
        document.querySelector("select[name='typeId']").value;

    if (!confirm("정말 삭제하시겠습니까?")) {
        return;
    }

    fetch(`/evaluator/delete?deptId=${deptId}&typeId=${typeId}&evaluateeNo=${empNo}`, {
        method: "POST"
    })
	.then(res => res.json())
	.then(data => {

	    if (!data.success) {
	        alert(data.message);
	        return;
	    }

	    location.href = `/evaluator?deptId=${deptId}&typeId=${typeId}`;
	});
}