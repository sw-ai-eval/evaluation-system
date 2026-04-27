/**
 * 평가요소 관리 페이지 전용 스크립트
 */

document.addEventListener("DOMContentLoaded", function() {
    window.calculateTotal();

    const saveWeightBtn = document.getElementById('btnSaveWeight');
    if (saveWeightBtn) {
        saveWeightBtn.onclick = saveWeightsToServer;
    }

    const saveGradeBtn = document.getElementById('btnSaveGrade');
    if (saveGradeBtn) {
        saveGradeBtn.onclick = saveGradesToServer;
    }
});

function switchTab(tabId) {
    const tabItems = document.querySelectorAll('.tab-item');
    tabItems.forEach(item => {
        item.classList.remove('active');
    });

    if (window.event && window.event.currentTarget) {
        window.event.currentTarget.classList.add('active');
    }

    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => {
        content.classList.remove('active');
    });

    const activeContent = document.getElementById('tab-' + tabId);
    if (activeContent) {
        activeContent.classList.add('active');
    }
}

window.calculateTotal = function() {
    const inputs = document.querySelectorAll('.weight-input');
    const alertBox = document.getElementById('weightTotalAlert');
    const sumSpan = document.getElementById('weightTotalSum');
    const saveBtn = document.getElementById('btnSaveWeight');
    
    if (!alertBox || !sumSpan || !saveBtn) {
        return;
    }

    let total = 0;
    inputs.forEach(input => {
        let val = parseInt(input.value) || 0;
        total += val;
    });
    
    sumSpan.innerText = total;
    
    if(total === 100) {
        alertBox.className = "weight-status status-ok";
        alertBox.style.backgroundColor = "#dcfce7";
        alertBox.style.color = "#166534";
        alertBox.innerHTML = `✅ 합계: <span>${total}</span>% (저장 가능)`;
        saveBtn.disabled = false;
        saveBtn.style.opacity = "1";
        saveBtn.style.cursor = "pointer";
    } else {
        alertBox.className = "weight-status status-err";
        alertBox.style.backgroundColor = "#fee2e2";
        alertBox.style.color = "#991b1b";
        alertBox.innerHTML = `⚠️ 합계: <span>${total}</span>% (100%가 되어야 합니다!)`;
        saveBtn.disabled = true;
        saveBtn.style.opacity = "0.5";
        saveBtn.style.cursor = "not-allowed";
    }
};

function saveWeightsToServer() {
    const deptId = document.getElementById('deptSelect').value;
    
    if (!deptId) {
        alert("대상 부서를 먼저 선택해주세요!");
        return;
    }

    const weights = [];
    const inputs = document.querySelectorAll('.weight-input');
    
    inputs.forEach((input, index) => {
        weights.push({
            deptId: deptId,
            typeId: index + 1, 
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
            alert("✅ 가중치 설정이 성공적으로 저장되었습니다!");
        } else {
            alert("❌ 저장 실패: " + data);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert("서버 통신 중 오류가 발생했습니다.");
    });
}

function saveGradesToServer() {
    const deptId = document.getElementById('deptSelect').value;
    
    if (!deptId) {
        alert("대상 부서를 먼저 선택해주세요!");
        return;
    }

    const grades = {
        deptId: deptId,
        gradeA: document.getElementById('gradeA').value,
        gradeB: document.getElementById('gradeB').value,
        gradeC: document.getElementById('gradeC').value
    };

    console.log("저장될 등급 데이터:", grades);
    alert("✅ [" + deptId + "] 부서의 등급 기준이 성공적으로 저장되었습니다!");
}

function loadDeptSettings(deptId) {
    if(!deptId) {
        return;
    }
    
    const weightInputs = document.querySelectorAll('.weight-input');
    const gradeA = document.getElementById('gradeA');
    const gradeB = document.getElementById('gradeB');
    const gradeC = document.getElementById('gradeC');

    if(deptId.includes('S')) { 
        weightInputs[0].value = 30; 
        weightInputs[1].value = 30; 
        weightInputs[2].value = 40;
        gradeA.value = 30; 
        gradeB.value = 70; 
        gradeC.value = 100;
    } else {
        weightInputs[0].value = 20; 
        weightInputs[1].value = 50; 
        weightInputs[2].value = 30;
        gradeA.value = 20; 
        gradeB.value = 50; 
        gradeC.value = 100;
    }
    
    window.calculateTotal(); 
}

window.openModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'flex';
    }
};

window.closeModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
};

function getFormattedDate(dateObj) {
    const yyyy = dateObj.getFullYear();
    const mm = String(dateObj.getMonth() + 1).padStart(2, '0');
    const dd = String(dateObj.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}

// 🌟 종료일 달력의 최소 선택 가능 범위를 업데이트하는 함수
window.updateMinEndDate = function(startDate) {
    const endDateInput = document.getElementById('newTypeEndDate');
    if (endDateInput) {
        endDateInput.min = startDate; // 시작일 이전은 클릭 불가
        // 만약 기존에 입력된 종료일이 새로운 시작일보다 빠르다면 초기화
        if (endDateInput.value && endDateInput.value < startDate) {
            endDateInput.value = startDate;
        }
    }
};

window.openNewTypeModal = function() {
    document.getElementById('typeId').value = '';
    document.getElementById('newTypeName').value = '';
    document.getElementById('newTypeYear').value = new Date().getFullYear();
    
    const today = new Date();
    const nextMonth = new Date();
    nextMonth.setMonth(nextMonth.getMonth() + 1);
    
    const todayStr = getFormattedDate(today);
    const nextMonthStr = getFormattedDate(nextMonth);
    
    document.getElementById('newTypeStartDate').value = todayStr;
    document.getElementById('newTypeEndDate').value = nextMonthStr;
    document.getElementById('newTypeStatus').value = 'true'; 
    
    // 🌟 팝업 열 때 종료일 제한 세팅
    window.updateMinEndDate(todayStr);
    
    document.getElementById('typeModalTitle').innerText = '➕ 평가 유형 등록';
    document.getElementById('typeModal').style.display = 'flex';
};

window.openEditTypeModal = function(id, name, year, startDate, endDate, statusStr) {
    document.getElementById('typeId').value = id;
    document.getElementById('newTypeName').value = name;
    document.getElementById('newTypeYear').value = year;
    document.getElementById('newTypeStartDate').value = startDate;
    document.getElementById('newTypeEndDate').value = endDate;
    document.getElementById('newTypeStatus').value = statusStr; 
    
    // 🌟 수정 팝업 열 때도 시작일 기준으로 종료일 제한 세팅
    window.updateMinEndDate(startDate);
    
    document.getElementById('typeModalTitle').innerText = '✏️ 평가 유형 수정';
    document.getElementById('typeModal').style.display = 'flex';
};

window.saveTypeToServer = function() {
    const id = document.getElementById('typeId').value;
    const name = document.getElementById('newTypeName').value;
    const year = document.getElementById('newTypeYear').value;
    const startDate = document.getElementById('newTypeStartDate').value;
    const endDate = document.getElementById('newTypeEndDate').value;
    const status = document.getElementById('newTypeStatus').value === 'true';

    if (!name || !startDate || !endDate) {
        alert("유형명, 시작일, 종료일을 모두 입력해주세요!");
        return;
    }

    // 날짜 역전 최종 검증 (혹시 모를 우회 방지)
    if (new Date(startDate) > new Date(endDate)) {
        alert("⚠️ 오류: 종료일은 시작일보다 빠를 수 없습니다!");
        return;
    }

    const evalTypeData = {
        name: name,
        year: parseInt(year),
        startDate: startDate + "T00:00:00", 
        endDate: endDate + "T23:59:59",
        status: status 
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
            alert("✅ 저장되었습니다!");
            location.reload(); 
        } else {
            alert("❌ 실패: " + data);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert("서버 통신 중 오류가 발생했습니다.");
    });
};

window.deleteTypeFromServer = function(id) {
    if (!confirm("정말 이 평가 유형을 삭제하시겠습니까?\n(가중치 등에 사용 중인 유형은 삭제할 수 없습니다.)")) {
        return; 
    }

    fetch('/evaluation/delete-type/' + id, {
        method: 'POST'
    })
    .then(response => response.text())
    .then(data => {
        if (data === "success") {
            alert("✅ 삭제되었습니다!");
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

// --- [평가 문항 관리] 관련 함수들 ---

// 신규 문항 팝업 열기
window.openNewItemModal = function() {
    document.getElementById('itemId').value = '';
    document.getElementById('itemCategory').value = '';
    document.getElementById('itemContent').value = '';
    document.getElementById('itemSortOrder').value = '10'; // 기본 정렬값
    document.getElementById('itemAnswerType').value = 'SCALE';
    document.getElementById('itemIsCommon').value = 'Y';
    
    document.getElementById('itemModalTitle').innerText = '📝 평가 문항 추가';
    window.openModal('questionModal');
};

// 기존 문항 수정 팝업 열기 (파라미터를 하나씩 받거나 객체로 처리)
window.openEditItemModal = function(id, typeId, category, content, answerType, isCommon, sortOrder) {
    document.getElementById('itemId').value = id;
    document.getElementById('itemTypeId').value = typeId;
    document.getElementById('itemCategory').value = category;
    document.getElementById('itemContent').value = content;
    document.getElementById('itemAnswerType').value = answerType;
    document.getElementById('itemIsCommon').value = isCommon;
    document.getElementById('itemSortOrder').value = sortOrder;
    
    document.getElementById('itemModalTitle').innerText = '✏️ 평가 문항 수정';
    window.openModal('questionModal');
};

// 문항 서버 저장
window.saveItemToServer = function() {
    const data = {
        id: document.getElementById('itemId').value,
        typeId: document.getElementById('itemTypeId').value,
        category: document.getElementById('itemCategory').value,
        content: document.getElementById('itemContent').value,
        answerType: document.getElementById('itemAnswerType').value,
        isCommon: document.getElementById('itemIsCommon').value,
        sortOrder: document.getElementById('itemSortOrder').value
    };

    if (!data.typeId) { alert("평가 유형을 선택해주세요."); return; }
    if (!data.content) { alert("문항 내용을 입력해주세요."); return; }

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
    });
};

// 문항 삭제
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
    });
};