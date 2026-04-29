console.log("department.js loaded");

function deleteDepartment() {
    const id = document.getElementById("edit-id").value;

    if (!confirm("정말 삭제하시겠습니까?")) {
        return;
    }

    fetch(`/department/${id}/delete`, {
        method: "POST"
    })
    .then(response => response.text())
    .then(msg => {
        alert(msg);
        location.reload();
    })
    .catch(err => {
        console.error(err);
        alert("에러 발생");
    });
}


function closeForm() {
    document.getElementById("createForm").style.display = "none";
    document.getElementById("editForm").style.display = "none";
    document.getElementById("formOverlay").style.display = "none";
}

function showCreateForm() {
	closeForm();


    const create = document.getElementById("createForm");
    const edit = document.getElementById("editForm");

    if (edit) edit.style.display = "none";
    if (create) create.style.display = "block";

}

function showEditForm(btn) {

    const d = btn.dataset;

    const set = (id, value) => {
        const el = document.getElementById(id);
        if (!el) return;
        el.value = value ?? "";
    };

    set("edit-id", d.id);
    set("edit-name", d.name);
    set("edit-useYn", d.useyn === "true" ? "true" : "false");

    const leaderSelect = document.getElementById("edit-leaderEmpNo");

    // 기본값 먼저 세팅
    if (leaderSelect) {
        leaderSelect.innerHTML = `<option value="">선택 안 함</option>`;
    }

    // 직원 목록 로딩
    fetch(`/department/${d.id}/employees`)
        .then(res => res.json())
        .then(list => {

            list.forEach(emp => {
                const opt = document.createElement('option');
                opt.value = emp.empNo;
                opt.textContent = emp.name;
                leaderSelect.appendChild(opt);
            });

            // 🔥 여기서 반드시 설정 (옵션 생성 후)
            leaderSelect.value = d.leaderempno || "";
        });

    set("edit-parentId", d.parentid || "");
    set("edit-parentName", d.parentname || "");

    const form = document.querySelector("#editForm form");

    // ❌ removeAttribute 제거 → 대신 value 유지
    form.onsubmit = (e) => {

        const leader = document.getElementById("edit-leaderEmpNo");

        // FK 방지: 빈 값이면 null로 보내기
        if (leader.value === "") {
            leader.value = ""; // 유지 (삭제하지 않음)
        }
    };

    document.getElementById("createForm").style.display = "none";
    document.getElementById("editForm").style.display = "block";
    document.getElementById("formOverlay").style.display = "block";
}

function submitUpdateForm() {

    const form = document.querySelector("#editForm form");
    const formData = new FormData(form);

    const leader = document.getElementById("edit-leaderEmpNo");

	if (!leader.value) {
	    formData.set("leaderEmpNo", "");
	}

	fetch("/department/update", {
	    method: "POST",
	    body: formData
	})
	.then(async res => {
	    const text = await res.text();

	    try {
	        return JSON.parse(text);
	    } catch (e) {
	        throw new Error(text);
	    }
	})
	.then(data => {
	    alert(data.message);

	    if (data.success) {
	        document.getElementById("editForm").style.display = "none";
	        document.getElementById("formOverlay").style.display = "none";
	        location.reload();
	    }
	})
	.catch(err => {
	    console.error(err);
	    alert("서버 오류");
	});
}

document.addEventListener("DOMContentLoaded", () => {

    const form = document.querySelector("#editForm form");

    form.addEventListener("submit", function(e) {
        e.preventDefault();
        submitUpdateForm();
    });
});


function toggleChildren(id) {
    const el = document.getElementById("child-" + id);
    if (!el) return;

    const isHidden = window.getComputedStyle(el).display === "none";
    el.style.display = isHidden ? "block" : "none";
}