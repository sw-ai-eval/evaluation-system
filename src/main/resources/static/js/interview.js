 // 등록 패널 OPEN
function openCreatePanel() {

    document
        .getElementById('panelTitle')
        .innerText = '면담 등록';

    // 초기화
    document.getElementById('panelEmployee').value = '';
    document.getElementById('panelType').value = '정기 면담';
    document.getElementById('panelDate').value = '';
    document.getElementById('panelLocation').value = '';

    document
    .getElementById('interviewPanel')
    .classList.add('open');
}

    // 수정 패널 OPEN
function openEditPanel(button) {

    document
        .getElementById('panelTitle')
        .innerText = '면담 수정';

    // 데이터 추출
    const date = button.dataset.date;
    const type = button.dataset.type;
    const emp = button.dataset.emp;
    const location = button.dataset.location;

    // 값 세팅
    document.getElementById('panelEmployee').value = emp;
    document.getElementById('panelType').value = type;
    document.getElementById('panelDate').value = formatDate(date);
    document.getElementById('panelLocation').value = location;

    // 패널 OPEN
    document
    .getElementById('interviewPanel')
    .classList.add('open');
}

    // 패널 닫기
function closeInterviewPanel() {

    document
        .getElementById('interviewPanel')
        .classList.remove('open');
}

    // datetime-local 형식 변환
function formatDate(dateString) {

    if (!dateString) return '';

    const date = new Date(dateString);

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day}T${hours}:${minutes}`;
}

