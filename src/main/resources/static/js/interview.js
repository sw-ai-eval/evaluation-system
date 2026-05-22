const topicContainer = document.getElementById('topicLabelContainer');
const typeSelect = document.getElementById('panelType');
let selectedTopics = [];
let currentLabels = [];


const startInput = document.getElementById("panelStartTime");
const endInput = document.getElementById("panelEndTime");

function validateEndTime() {
    const startDateTime = startInput.value; // yyyy-MM-ddTHH:mm
    const endTime = endInput.value; // HH:mm

    if (!startDateTime || !endTime) return;

    // 시작 날짜
    const startDate = startDateTime.split("T")[0];
    const startTime = startDateTime.split("T")[1];

    const start = new Date(`${startDate}T${startTime}`);
    const end = new Date(`${startDate}T${endTime}`);

    if (end <= start) {
        alert("종료 시각은 시작 시각보다 늦어야 합니다.");
        endInput.value = ""; // 잘못된 값 초기화
    }
}
endInput.addEventListener("change", validateEndTime);

// 시작 시간 변경 시 종료 시간이 이미 선택되어 있으면 체크
startInput.addEventListener("change", () => {
    if (endInput.value) validateEndTime();
});

// 라벨 렌더링 이벤트
typeSelect.addEventListener('change', () => {
    const selectedTypeId = typeSelect.value;

    selectedTopics = []; // 🔥 타입 바뀌면 초기화

    if (!selectedTypeId) {
        renderTopicLabels([]);
        return;
    }

    fetch(`/interview/labels?typeId=${encodeURIComponent(selectedTypeId)}`)
        .then(res => res.json())
        .then(labels => {
            currentLabels = labels;
            renderTopicLabels(labels);
        })
        .catch(err => console.error(err));
});

// 서버에서 받아온 라벨 렌더링
function renderTopicLabelsFromServer(labels) {
    renderTopicLabels(labels, []); // 선택 라벨 없음
}

function renderTopicLabels(labels, isReadOnly = false) {
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

        // 🔥 선택 상태
        if (selectedTopics.includes(labelText)) {
            span.classList.add('selected');
        }

        // 읽기 전용이 아닐 때만 클릭 이벤트 등록
        if (!isReadOnly) {
            span.addEventListener('click', () => {

                span.classList.toggle('selected');

                if (span.classList.contains('selected')) {
                    if (!selectedTopics.includes(labelText)) {
                        selectedTopics.push(labelText);
                    }
                } else {
                    selectedTopics = selectedTopics.filter(t => t !== labelText);
                }

                console.log("selectedTopics:", selectedTopics);
            });
        } else {
            // 읽기 전용이면 클릭 불가 스타일
            span.style.cursor = 'default';
        }

        topicContainer.appendChild(span);
    });
}

// 등록 패널 OPEN
function openCreatePanel() {
    document.getElementById('panelTitle').innerText = '면담 등록';

    selectedTopics = [];
    currentLabels = [];

    document.getElementById('panelEmployee').value = '';
    document.getElementById('panelType').value = '';
    document.getElementById('panelStartTime').value = '';
    document.getElementById('panelEndTime').value = '';
    document.getElementById('panelPlace').value = '';
	const deleteBtn = document.querySelector('.delete-btn');
	deleteBtn.style.display = 'none';   

    renderTopicLabels([]);

    document.getElementById('interviewPanel').classList.add('open');
}
// 수정 패널 OPEN
function openEditPanel(button, isReadOnly = false) {
    const id = button.dataset.id;
    console.log("🔥 interview id:", id);

    document.getElementById('panelTitle').innerText = isReadOnly ? '면담 조회' : '면담 수정';

    // 상태 초기화
    selectedTopics = [];

    // 삭제 버튼 element
	const deleteBtn = document.querySelector('.delete-btn');
	if (deleteBtn) deleteBtn.style.display = isReadOnly ? 'none' : 'inline-block';

	const saveBtn = document.querySelector('.save-btn');
	if (saveBtn) saveBtn.style.display = isReadOnly ? 'none' : 'inline-block';

    fetch(`/interview/detail?id=${id}`)
        .then(res => res.json())
        .then(data => {
            console.log("📥 detail data:", data);
            document.getElementById('panelInterviewId').value = id;

            // 기본 값 세팅
            document.getElementById('panelEmployee').value = data.evaluateeNo;
            document.getElementById('panelStartTime').value = data.startDateTime;
            document.getElementById('panelEndTime').value = data.endDateTime.split('T')[1];
            document.getElementById('panelPlace').value = data.place;
            document.getElementById('panelType').value = data.interviewType.toString();
			document.getElementById('panelStatus').value = data.status;

            // topics 세팅
            selectedTopics = data.topics || [];

            // 라벨 조회
            return fetch(`/interview/labels?typeId=${data.interviewType}`);
        })
        .then(res => res.json())
        .then(labels => {
            currentLabels = labels;
            renderTopicLabels(labels, isReadOnly); // 읽기 전용 여부 전달
        })
        .catch(err => console.error("❌ error:", err));

    // 읽기 전용이면 input, select, textarea 비활성화
	if (isReadOnly) {
	    const selectElements = [
	        'panelEmployee', 
	        'panelType', 
	        'panelStatus'
	    ];

	    selectElements.forEach(id => {
	        const el = document.getElementById(id);
	        el.classList.add('readonly-select');
	        el.disabled = false; // disabled 대신 클래스만 사용
	    });

	    document.getElementById('panelStartTime').disabled = true;
	    document.getElementById('panelEndTime').disabled = true;
	    document.getElementById('panelPlace').disabled = true;
	    document.getElementById('panelTextarea').readOnly = true;

	    // 저장 버튼 숨기기
	    const saveBtn = document.querySelector('.save-btn');
	    saveBtn.style.display = 'none';
	} else {
	    const selectElements = [
	        'panelEmployee', 
	        'panelType', 
	        'panelStatus'
	    ];

	    selectElements.forEach(id => {
	        const el = document.getElementById(id);
	        el.classList.remove('readonly-select');
	        el.disabled = false;
	    });

	    document.getElementById('panelStartTime').disabled = false;
	    document.getElementById('panelEndTime').disabled = false;
	    document.getElementById('panelPlace').disabled = false;
	    document.getElementById('panelTextarea').readOnly = false;
	    document.querySelector('.save-btn').style.display = 'inline-block';
	}

    document.getElementById('interviewPanel').classList.add('open');
}


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
function getSelectedTopics() {
    const selected = [];

    document.querySelectorAll('#topicLabelContainer .topic-label.selected')
        .forEach(el => {
            selected.push(el.textContent.trim());
        });

    return selected;
}

function saveInterview() {
    const employee = document.getElementById("panelEmployee").value;
    const type = document.getElementById("panelType").value;
    const topics = getSelectedTopics();
	const startDateTime = document.getElementById("panelStartTime").value; // yyyy-MM-ddTHH:mm
	const endTime = document.getElementById("panelEndTime").value; // HH:mm
	
	let endDateTime = "";
	if (startDateTime && endTime) {
	    const startDate = startDateTime.split("T")[0]; // yyyy-MM-dd
	    endDateTime = `${startDate}T${endTime}`;
	}

	
    // 🔥 필수 체크
    if (!employee) {
        alert("면담 대상자를 선택해주세요.");
        document.getElementById("panelEmployee").focus();
        return;
    }

    if (!type) {
        alert("면담 구분을 선택해주세요.");
        document.getElementById("panelType").focus();
        return;
    }

    if (!topics || topics.length === 0) {
        alert("면담 주제를 최소 1개 이상 선택해주세요.");
        return;
    }

    const data = {
		id: document.getElementById('panelInterviewId').value,
        evaluatee: employee,
        interviewType: type,
        start: startDateTime,
        end: endDateTime,
        place: document.getElementById("panelPlace").value,
        status: document.getElementById("panelStatus").value,
        textarea: document.getElementById("panelTextarea").value,
        topics: topics
    };

	fetch("/api/interview/save", {
	        method: "POST",
	        headers: { "Content-Type": "application/json" },
	        body: JSON.stringify(data)
	    })
	    .then(res => {
	        console.log("📥 response status:", res.status);
	        return res.text();
	    })
	    .then(text => {
	        console.log("📥 response body:", text);
			window.location.href = "/interview";
	    })
	    .catch(err => {
	        console.error("❌ fetch error:", err);
	    });
}

function deleteInterview(){
	const data = {
		id: document.getElementById('panelInterviewId').value,
	};
	fetch("/api/interview/delete", {
		        method: "POST",
		        headers: { "Content-Type": "application/json" },
		        body: JSON.stringify(data)
		    })
		    .then(res => {
		        console.log("📥 response status:", res.status);
		        return res.text();
		    })
		    .then(text => {
		        console.log("📥 response body:", text);
				window.location.href = "/interview";
		    })
		    .catch(err => {
		        console.error("❌ fetch error:", err);
		    });
}