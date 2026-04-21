console.log("department.js loaded");

function showCreateForm() {
    const empty = document.getElementById("emptyFormMessage");
    const create = document.getElementById("createForm");
    const edit = document.getElementById("editForm");

    if (empty) empty.style.display = "none";
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

    const empty = document.getElementById("emptyFormMessage");
    const create = document.getElementById("createForm");
    const edit = document.getElementById("editForm");

    if (empty) empty.style.display = "none";
    if (create) create.style.display = "none";
    if (edit) edit.style.display = "block";
}

function toggleChildren(id) {
    const el = document.getElementById("child-" + id);
    if (!el) return;

    const isHidden = window.getComputedStyle(el).display === "none";
    el.style.display = isHidden ? "block" : "none";
}