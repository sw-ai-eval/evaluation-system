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
    console.log("dataset:", btn.dataset);

    const d = btn.dataset;

    const set = (id, value) => {
        const el = document.getElementById(id);
        if (!el) {
            console.warn("❌ 요소 없음:", id);
            return;
        }
        el.value = value ?? "";
    };

    set("edit-id", d.id);
    set("edit-name", d.name);
    set("edit-leaderEmpNo", d.leaderempno);
    set("edit-parentId", d.parentid || "");
    set("edit-useYn", d.useyn);
	
	requestAnimationFrame(() => {
	    const select = document.getElementById("edit-parentId");

	    Array.from(select.options).forEach(opt => {
	        opt.disabled = (opt.value === d.id);
	    });
	});
	

    // ✅ 직원 리스트 조회 (핵심 수정 부분)
    fetch(`/department/${d.id}/employees`)
        .then(res => {
            if (!res.ok) throw new Error("직원 조회 실패");
            return res.json();
        })
        .then(list => {
            const select = document.querySelector('#edit-leaderEmpNo');

            if (!select) {
                console.warn("❌ select 없음: edit-leaderEmpNo");
                return;
            }

            select.innerHTML = `<option value="">선택 안 함</option>`;

            list.forEach(emp => {
                const opt = document.createElement('option');
                opt.value = emp.empNo;
                opt.textContent = emp.name;
                select.appendChild(opt);
            });
			select.value = d.leaderempno || "";
        })
        .catch(err => {
            console.error("❌ 직원 목록 조회 오류:", err);
        });
	
	const create = document.getElementById("createForm");
	const edit = document.getElementById("editForm");

	if (create) create.style.display = "none";
	if (edit) edit.style.display = "block";

	document.getElementById("formOverlay").style.display = "block";
}

function toggleChildren(id) {
    const el = document.getElementById("child-" + id);
    if (!el) return;

    const isHidden = window.getComputedStyle(el).display === "none";
    el.style.display = isHidden ? "block" : "none";
}