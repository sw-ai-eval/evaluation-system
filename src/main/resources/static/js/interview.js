const topicContainer = document.getElementById('topicLabelContainer');
const typeSelect = document.getElementById('panelType');

// 라벨 렌더링 이벤트
typeSelect.addEventListener('change', () => {
    const selectedTypeId = typeSelect.value;

    if (!selectedTypeId) {
        renderTopicLabels([], []); // 초기화
        return;
    }

    fetch(`/interview/labels?typeId=${encodeURIComponent(selectedTypeId)}`)
        .then(response => response.json())
        .then(labels => renderTopicLabelsFromServer(labels))
        .catch(err => console.error(err));
});

// 서버에서 받아온 라벨 렌더링
function renderTopicLabelsFromServer(labels) {
    renderTopicLabels(labels, []); // 선택 라벨 없음
}

// ✅ 새로 정의: 라벨 렌더링 공용 함수
function renderTopicLabels(labels, selectedTopics = []) {
    const topicContainer = document.getElementById('topicLabelContainer');
    topicContainer.innerHTML = '';

    if (!labels || labels.length === 0 || !labels[0]?.categoryLabels) {
        const msg = document.createElement('div');
        msg.className = 'no-label-message';
        msg.textContent = '선택 가능한 주제가 없습니다.';
        topicContainer.appendChild(msg);
        return;
    }

    labels[0].categoryLabels.forEach(labelText => {
        const span = document.createElement('span');
        span.className = 'topic-label';
        span.textContent = labelText;

        // 기존 선택된 라벨 표시
        if (selectedTopics.includes(labelText)) {
            span.classList.add('selected');
        }

        span.addEventListener('click', () => {
            span.classList.toggle('selected');
        });

        topicContainer.appendChild(span);
    });
}

// 등록 패널 OPEN
function openCreatePanel() {
    document.getElementById('panelTitle').innerText = '면담 등록';

    document.getElementById('panelEmployee').value = '';
    document.getElementById('panelType').value = '';
    document.getElementById('panelStartTime').value = '';
    document.getElementById('panelEndTime').value = '';
    document.getElementById('panelLocation').value = '';

    renderTopicLabels([], []); // 라벨 초기화

    document.getElementById('interviewPanel').classList.add('open');
}

// 수정 패널 OPEN
function openEditPanel(button) {
    document.getElementById('panelTitle').innerText = '면담 수정';

    const start = button.dataset.start;
    const end = button.dataset.end;
    const type = button.dataset.type;
    const emp = button.dataset.emp;
    const location = button.dataset.location;
    const selectedTopics = button.dataset.topics ? button.dataset.topics.split(',') : [];

    document.getElementById('panelEmployee').value = emp;
    document.getElementById('panelType').value = type;
    document.getElementById('panelStartTime').value = formatDate(start);
    document.getElementById('panelEndTime').value = formatDate(end);
    document.getElementById('panelLocation').value = location;

    if (type) {
        fetch(`/interview/labels?typeId=${encodeURIComponent(type)}`)
            .then(response => response.json())
            .then(labels => renderTopicLabels(labels, selectedTopics))
            .catch(err => console.error(err));
    } else {
        renderTopicLabels([], selectedTopics);
    }

    document.getElementById('interviewPanel').classList.add('open');
}
const statusSelect = document.getElementById('panelStatus');

statusSelect.addEventListener('change', function() {
    statusSelect.classList.remove('pending', 'delayed', 'completed');

    const value = statusSelect.value; // pending / delayed / completed
    statusSelect.classList.add(value);
});
// 패널 닫기
function closeInterviewPanel() {
    document.getElementById('interviewPanel').classList.remove('open');
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