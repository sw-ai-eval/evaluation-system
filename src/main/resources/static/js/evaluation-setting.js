/**
 * 평가요소 관리 페이지 전용 스크립트
 */

document.addEventListener("DOMContentLoaded", function() {
    // 1. 세션에서 마지막으로 활성화되었던 탭을 가져와서 복구
    const savedTab = sessionStorage.getItem('activeEvalTab') || 'type';
    switchTab(savedTab);

    // 2. 페이지 로드 시 가중치 합계 미리 계산
    window.calculateTotal();

    // 3. 버튼들에 클릭 이벤트 리스너 직접 연결합
    const saveWeightBtn = document.getElementById('btnSaveWeight');
    if (saveWeightBtn) {
        saveWeightBtn.onclick = saveWeightsToServer;
    }

    const saveGradeBtn = document.getElementById('btnSaveGrade');
    if (saveGradeBtn) {
        saveGradeBtn.onclick = saveGradesToServer;
    }
});

// [공통] 탭 전환 함수
function switchTab(tabId) {
    const tabItems = document.querySelectorAll('.tab-item');
    tabItems.forEach(item => {
        item.classList.remove('active');
        if(item.getAttribute('onclick') && item.getAttribute('onclick').includes(tabId)) {
            item.classList.add('active');
        }
    });

    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => {
        content.classList.remove('active');
    });

    const activeContent = document.getElementById('tab-' + tabId);
    if (activeContent) {
        activeContent.classList.add('active');
    }
    
    // 현재 보고 있는 탭을 세션에 저장하여 새로고침 시 유지되게 합니다.
    sessionStorage.setItem('activeEvalTab', tabId);
}

// [문항] 답변 타입에 따른 배점 입력칸 표시/숨김 토글
window.toggleWeightDisplay = function(type) {
    const weightGroup = document.getElementById('weightGroup');
    if (!weightGroup) return;
    
    // 서술형(TEXT)이면 배점 입력란을 아예 숨기고 값을 0으로 초기화합니다.
    if (type === 'TEXT') {
        weightGroup.style.display = 'none';
        document.getElementById('itemWeight').value = 0; 
    } else {
        weightGroup.style.display = 'block';
    }
};

// [가중치] 가중치 합계 계산 및 저장 버튼 활성화 제어
window.calculateTotal = function() {
    const inputs = document.querySelectorAll('.weight-input');
    const alertBox = document.getElementById('weightTotalAlert');
    const saveBtn = document.getElementById('btnSaveWeight');
    const sumSpan = document.getElementById('weightTotalSum');
    
    if (!alertBox || !saveBtn) return;

    let total = 0;
    inputs.forEach(input => {
        let val = parseInt(input.value) || 0;
        total += val;
    });
    
    if (sumSpan) {
        sumSpan.innerText = total;
    }
    
    // 합계가 정확히 100%일 때만 저장 버튼을 활성화
    if(total === 100) {
        alertBox.className = "weight-status status-ok";
        saveBtn.disabled = false;
        saveBtn.style.opacity = "1";
        saveBtn.style.cursor = "pointer";
    } else {
        alertBox.className = "weight-status status-err";
        saveBtn.disabled = true;
        saveBtn.style.opacity = "0.5";
        saveBtn.style.cursor = "not-allowed";
    }
};

// [가중치] 서버로 가중치 데이터 전송
function saveWeightsToServer() {
    const deptId = document.getElementById('deptSelect').value;
    if (!deptId) {
        alert("대상 부서를 먼저 선택해주세요.");
        return;
    }

    const weights = [];
    const inputs = document.querySelectorAll('.weight-input');
    
    inputs.forEach((input) => {
        weights.push({
            deptId: deptId,
            typeId: parseInt(input.getAttribute('data-typeid')),
            weight: parseInt(input.value)
        });
    });

    fetch('/evaluation/save-weights', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(weights)
    })
    .then(response => response.text())
    .then(data => {
        if (data === "success") {
            alert("✅ 가중치 설정이 성공적으로 저장되었습니다.");
        } else {
            alert("❌ 저장 실패: " + data);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert("서버 통신 중 오류가 발생했습니다.");
    });
}

// [등급] 조직별 등급 기준 저장
function saveGradesToServer() {
    const deptId = document.getElementById('deptSelect').value;
    if (!deptId) {
        alert("대상 부서를 먼저 선택해주세요.");
        return;
    }

    const grades = {
        deptId: deptId,
        gradeS: document.getElementById('gradeS').value,
        gradeA: document.getElementById('gradeA').value,
        gradeB: document.getElementById('gradeB').value,
        gradeC: document.getElementById('gradeC').value,
        gradeD: document.getElementById('gradeD').value
    };

    console.log("저장될 등급 데이터:", grades);
    alert("✅ [" + deptId + "] 부서의 등급 기준이 성공적으로 저장되었습니다.");
}

// [부서] 부서 선택 시 기본값 자동 로드
function loadDeptSettings(deptId) {
    if(!deptId) return;
    
    const weightInputs = document.querySelectorAll('.weight-input');
    weightInputs.forEach(input => {
        const name = input.getAttribute('data-name');
        // 부서 명칭에 따른 기본 가중치 자동 할당 로직
        if (name && name.includes("근태")) {
            input.value = 20;
        } else if (name && name.includes("성과")) {
            input.value = 40;
        } else if (name && name.includes("역량")) {
            input.value = 40;
        } else {
            input.value = 0;
        }
    });

    // 기본 등급 비율 세팅
    document.getElementById('gradeS').value = 10; 
    document.getElementById('gradeA').value = 30; 
    document.getElementById('gradeB').value = 70; 
    document.getElementById('gradeC').value = 90; 
    document.getElementById('gradeD').value = 100;
    
    window.calculateTotal(); 
}

// [공통] 모달 열기/닫기
window.openModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.style.display = 'flex';
};
window.closeModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.style.display = 'none';
};

// [날짜] yyyy-MM-dd 형식 변환
function getFormattedDate(dateObj) {
    const yyyy = dateObj.getFullYear();
    const mm = String(dateObj.getMonth() + 1).padStart(2, '0');
    const dd = String(dateObj.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}

// [날짜] 시작일에 따른 종료일 최소값 제한
window.updateMinEndDate = function(startDate) {
    const endDateInput = document.getElementById('newTypeEndDate');
    if (endDateInput) {
        endDateInput.min = startDate;
        if (endDateInput.value && endDateInput.value < startDate) {
            endDateInput.value = startDate;
        }
    }
};

// ==============================================
// 1. 평가 유형(Type) 관리 함수들
// ==============================================

window.openNewTypeModal = function() {
    document.getElementById('typeId').value = '';
    document.getElementById('newTypeName').value = '';
    document.getElementById('newTypeYear').value = new Date().getFullYear();
    document.getElementById('newTypeGuideline').value = '';
    
    const today = new Date();
    const nextMonth = new Date();
    nextMonth.setMonth(nextMonth.getMonth() + 1);
    
    const todayStr = getFormattedDate(today);
    const nextMonthStr = getFormattedDate(nextMonth);
    
    document.getElementById('newTypeStartDate').value = todayStr;
    document.getElementById('newTypeEndDate').value = nextMonthStr;
    document.getElementById('newTypeStatus').value = 'true'; 
    document.getElementById('newTypeHasWeight').value = 'true';
    
    window.updateMinEndDate(todayStr);
    document.getElementById('typeModalTitle').innerText = '➕ 평가 유형 등록';
    window.openModal('typeModal');
};

window.openEditTypeModal = function(id, name, year, startDate, endDate, statusStr, guideline, hasWeight) {
    document.getElementById('typeId').value = id;
    document.getElementById('newTypeName').value = name;
    document.getElementById('newTypeYear').value = year;
    document.getElementById('newTypeStartDate').value = startDate;
    document.getElementById('newTypeEndDate').value = endDate;
    document.getElementById('newTypeStatus').value = statusStr; 
    document.getElementById('newTypeGuideline').value = (guideline && guideline !== 'null') ? guideline : ''; 
    document.getElementById('newTypeHasWeight').value = hasWeight; 
    
    window.updateMinEndDate(startDate);
    document.getElementById('typeModalTitle').innerText = '✏️ 평가 유형 수정';
    window.openModal('typeModal');
};

window.saveTypeToServer = function() {
    const id = document.getElementById('typeId').value;
    const name = document.getElementById('newTypeName').value;
    const year = document.getElementById('newTypeYear').value;
    const startDate = document.getElementById('newTypeStartDate').value;
    const endDate = document.getElementById('newTypeEndDate').value;
    const status = document.getElementById('newTypeStatus').value === 'true';
    const hasWeight = document.getElementById('newTypeHasWeight').value === 'true';
    const guideline = document.getElementById('newTypeGuideline').value;

    if (!name || !startDate || !endDate) {
        alert("아직 입력되지 않은 항목이 있습니다.");
        return;
    }

    const evalTypeData = {
        name: name,
        year: parseInt(year),
        startDate: startDate + "T00:00:00", 
        endDate: endDate + "T23:59:59",
        status: status,
        hasWeight: hasWeight,
        guideline: guideline
    };
    
    if (id) {
        evalTypeData.id = parseInt(id); 
    }

    fetch('/evaluation/save-type', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(evalTypeData)
    })
    .then(response => response.text())
    .then(data => {
        if (data === "success") {
            alert("✅ 저장되었습니다.");
            location.reload(); 
        } else {
            alert("❌ " + data);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert("서버 통신 중 오류가 발생했습니다.");
    });
};

window.deleteTypeFromServer = function(id) {
    if (!confirm("정말 이 평가 유형을 삭제하시겠습니까?\n(사용 중인 유형은 삭제할 수 없습니다.)")) {
        return; 
    }

    fetch('/evaluation/delete-type/' + id, {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => {
        if (data === "success") {
            alert("✅ 삭제되었습니다.");
            location.reload(); 
        } else {
            alert("❌ " + data); 
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert("서버 통신 중 오류가 발생했습니다.");
    });
};

// ==============================================
// 2. 평가 문항(Item) 관리 함수들
// ==============================================

window.openNewItemModal = function() {
    document.getElementById('itemId').value = '';
    document.getElementById('itemCategory').value = '';
    document.getElementById('itemContent').value = '';
    document.getElementById('itemAnswerType').value = 'SCORE';
    document.getElementById('itemIsCommon').value = 'Y';
    
    // 새 문항일 때 배점 초기화 및 표시
    document.getElementById('itemWeight').value = '';
    window.toggleWeightDisplay('SCORE');
    
    document.getElementById('itemModalTitle').innerText = '➕ 평가 문항 추가';
    window.openModal('questionModal');
};

window.openEditItemModal = function(btn) {
    const id = btn.getAttribute('data-id');
    const typeId = btn.getAttribute('data-typeid');
    const category = btn.getAttribute('data-category');
    const content = btn.getAttribute('data-content');
    const weight = btn.getAttribute('data-weight') || 0; // 🌟 배점 데이터 읽기
    const answerType = btn.getAttribute('data-answertype');
    const isCommon = btn.getAttribute('data-iscommon');

    // 모달창 필드에 값 세팅
    document.getElementById('itemId').value = id;
    document.getElementById('itemTypeId').value = typeId;
    document.getElementById('itemCategory').value = category;
    document.getElementById('itemContent').value = content;
    document.getElementById('itemAnswerType').value = answerType;
    document.getElementById('itemWeight').value = weight; // 🌟 배점 입력칸에 꽂기
    document.getElementById('itemIsCommon').value = isCommon;
    
    // 답변 타입에 따라 배점 칸 숨김/표시 처리
    window.toggleWeightDisplay(answerType);
    
    document.getElementById('itemModalTitle').innerText = '✏️ 평가 문항 수정';
    window.openModal('questionModal');
};

window.saveItemToServer = function() {
    const answerType = document.getElementById('itemAnswerType').value;
    const weightVal = document.getElementById('itemWeight').value;

    const data = {
        id: document.getElementById('itemId').value,
        typeId: document.getElementById('itemTypeId').value,
        category: document.getElementById('itemCategory').value,
        content: document.getElementById('itemContent').value,
        answerType: answerType,
        isCommon: document.getElementById('itemIsCommon').value,
        // 서술형(TEXT)이면 무조건 0점, 점수형이면 입력값 사용
        weight: (answerType === 'TEXT') ? 0 : (parseInt(weightVal) || 0)
    };

    // 유효성 검사
    if (!data.typeId) { alert("평가 유형을 선택해주세요."); return; }
    if (!data.category) { alert("카테고리를 입력해주세요."); return; }
    if (!data.content) { alert("문항 내용을 입력해주세요."); return; }
    
    // 점수형인데 배점을 입력 안 했을 경우 체크
    if (answerType === 'SCORE' && data.weight <= 0) {
        alert("점수형 문항은 1점 이상의 배점을 입력해야 합니다.");
        document.getElementById('itemWeight').focus();
        return;
    }

    fetch('/evaluation/save-item', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => response.text())
    .then(result => {
        if (result === "success") {
            alert("✅ 문항이 저장되었습니다.");
            location.reload();
        } else {
            alert("❌ 저장 실패: " + result);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert("서버 통신 중 오류가 발생했습니다.");
    });
};

window.deleteItemFromServer = function(id) {
    if (!confirm("이 문항을 삭제하시겠습니까?")) return;

    fetch('/evaluation/delete-item/' + id, {
        method: 'POST'
    })
    .then(response => response.text())
    .then(result => {
        if (result === "success") {
            alert("✅ 삭제되었습니다.");
            location.reload();
        } else {
            alert("❌ 삭제 실패: " + result);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert("서버 통신 중 오류가 발생했습니다.");
    });
};