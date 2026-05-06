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