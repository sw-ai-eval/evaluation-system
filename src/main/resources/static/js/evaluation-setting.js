/**
 * 평가요소 관리 페이지 전용 스크립트
 */

document.addEventListener("DOMContentLoaded", function() {
    // 1. 세션에서 마지막으로 활성화되었던 탭을 가져와서 복구
    const savedTab = sessionStorage.getItem('activeEvalTab') || 'type';
    switchTab(savedTab);

    // 2. 페이지 로드 시 가중치 합계 미리 계산
    if(typeof window.calculateTotal === 'function') {
        window.calculateTotal();
    }

    // 3. 버튼들에 클릭 이벤트 리스너 직접 연결
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
    
    // 현재 보고 있는 탭을 세션에 저장하여 새로고침 시 유지
    sessionStorage.setItem('activeEvalTab', tabId);
}

// [문항] 답변 타입에 따른 배점 입력칸 표시/숨김 토글
window.toggleWeightDisplay = function(type) {
    const weightGroup = document.getElementById('weightGroup');
    if (!weightGroup) return;
    
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

    const applyToChildren = document.getElementById('applyToChildren').checked;

    fetch(`/evaluation/save-weights?applyToChildren=${applyToChildren}`, {
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

// [등급] 조직별 등급 기준 DB 저장
function saveGradesToServer() {
    const deptId = document.getElementById('deptSelect').value;
    
    if (!deptId) {
        alert("대상 부서를 먼저 선택해주세요.");
        return;
    }

    const grades = {
        deptId: deptId,
        gradeS: parseInt(document.getElementById('gradeS').value),
        gradeA: parseInt(document.getElementById('gradeA').value),
        gradeB: parseInt(document.getElementById('gradeB').value),
        gradeC: parseInt(document.getElementById('gradeC').value),
        gradeD: parseInt(document.getElementById('gradeD').value)
    };

    const applyToChildren = document.getElementById('applyGradeToChildren').checked;

    fetch(`/evaluation/save-grades?applyToChildren=${applyToChildren}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(grades)
    })
    .then(response => response.text())
    .then(data => {
        if (data === "success") {
            alert("✅ 등급 기준 설정이 성공적으로 저장되었습니다.");
        } else {
            alert("❌ 저장 실패: " + data);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert("서버 통신 중 오류가 발생했습니다.");
    });
}

// [부서] 부서 선택 시 가중치 및 등급 기준 DB 로드
function loadDeptSettings(deptId) {
    const weightInputs = document.querySelectorAll('.weight-input');

    if(!deptId) {
        weightInputs.forEach(input => input.value = 0);
        if(typeof window.calculateTotal === 'function') window.calculateTotal();
        resetGradeInputs();
        return;
    }
    
    fetch('/evaluation/weights/' + deptId)
        .then(response => response.json())
        .then(savedWeights => {
            weightInputs.forEach(input => {
                const typeId = parseInt(input.getAttribute('data-typeid'));
                const matched = savedWeights.find(w => w.typeId === typeId);
                input.value = matched ? matched.weight : 0;
            });
            if(typeof window.calculateTotal === 'function') window.calculateTotal();
        })
        .catch(error => console.error('가중치 로드 에러:', error));

    fetch('/evaluation/grades/' + deptId)
        .then(response => {
            if (response.ok) return response.json();
            return null;
        })
        .then(gradeData => {
            if (gradeData && Object.keys(gradeData).length > 0) {
                document.getElementById('gradeS').value = gradeData.gradeS;
                document.getElementById('gradeA').value = gradeData.gradeA;
                document.getElementById('gradeB').value = gradeData.gradeB;
                document.getElementById('gradeC').value = gradeData.gradeC;
                document.getElementById('gradeD').value = gradeData.gradeD;
            } else {
                resetGradeInputs();
            }
        })
        .catch(error => {
            console.error("등급 로드 에러:", error);
            resetGradeInputs();
        });
}

function resetGradeInputs() {
    document.getElementById('gradeS').value = 10;
    document.getElementById('gradeA').value = 30;
    document.getElementById('gradeB').value = 70;
    document.getElementById('gradeC').value = 90;
    document.getElementById('gradeD').value = 100;
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

function getFormattedDate(dateObj) {
    const yyyy = dateObj.getFullYear();
    const mm = String(dateObj.getMonth() + 1).padStart(2, '0');
    const dd = String(dateObj.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}

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
    
    if(document.getElementById('itemExplanation')) {
        document.getElementById('itemExplanation').value = '';
    }

    document.getElementById('itemAnswerType').value = 'SCORE';
    document.getElementById('itemIsCommon').value = 'Y';
    
    document.getElementById('itemWeight').value = '';
    window.toggleWeightDisplay('SCORE');
    
    // 특수문항 타겟 UI 초기화 (새로운 트리 구조 대응)
    if(typeof window.toggleTargetDisplay === 'function') {
        window.toggleTargetDisplay('Y');
        document.getElementById('targetDeptSelect').value = '';
        document.getElementById('targetEmpArea').style.display = 'none';
        document.getElementById('finalTargetList').innerHTML = ''; // 최종 리스트 싹 지우기
    }
    
    document.getElementById('itemModalTitle').innerText = '➕ 평가 문항 추가';
    window.openModal('questionModal');
};

window.openEditItemModal = function(btn) {
    const id = btn.getAttribute('data-id');
    const typeId = btn.getAttribute('data-typeid');
    const category = btn.getAttribute('data-category');
    const content = btn.getAttribute('data-content');
    const weight = btn.getAttribute('data-weight') || 0; 
    const answerType = btn.getAttribute('data-answertype');
    const isCommon = btn.getAttribute('data-iscommon');
    const explanation = btn.getAttribute('data-explanation'); 

    document.getElementById('itemId').value = id;
    document.getElementById('itemTypeId').value = typeId;
    document.getElementById('itemCategory').value = category;
    document.getElementById('itemContent').value = content;
    document.getElementById('itemAnswerType').value = answerType;
    document.getElementById('itemWeight').value = weight; 
    document.getElementById('itemIsCommon').value = isCommon;
    
    if(document.getElementById('itemExplanation')) {
        document.getElementById('itemExplanation').value = (explanation && explanation !== 'null') ? explanation : '';
    }
    
    window.toggleWeightDisplay(answerType);
    
    if(typeof window.toggleTargetDisplay === 'function') {
        window.toggleTargetDisplay(isCommon);
        // 향후: 기존에 저장된 타겟 데이터를 불러와서 finalTargetList에 그려주는 로직이 필요합니다.
    }
    
    document.getElementById('itemModalTitle').innerText = '✏️ 평가 문항 수정';
    window.openModal('questionModal');
};

window.saveItemToServer = function() {
    const answerType = document.getElementById('itemAnswerType').value;
    const weightVal = document.getElementById('itemWeight').value;
    const isCommonVal = document.getElementById('itemIsCommon').value;
    
    let explanationValue = null;
    if(document.getElementById('itemExplanation')) {
        const text = document.getElementById('itemExplanation').value.trim();
        if(text !== "") explanationValue = text;
    }

    // 🌟 새로운 방식의 타겟 데이터 수집
    let targets = [];
    if (isCommonVal === 'N') {
        const listItems = document.querySelectorAll('#finalTargetList li');
        listItems.forEach(li => {
            targets.push({
                targetType: li.getAttribute('data-type'),
                targetValue: li.getAttribute('data-value')
            });
        });
        
        if (targets.length === 0) {
            alert('특수 문항을 적용할 대상(부서 전체 또는 특정 사원)을 최소 1개 이상 아래 리스트에 추가해주세요.');
            return;
        }
    }

    const data = {
        id: document.getElementById('itemId').value,
        typeId: document.getElementById('itemTypeId').value,
        category: document.getElementById('itemCategory').value,
        content: document.getElementById('itemContent').value,
        answerType: answerType,
        isCommon: isCommonVal,
        weight: (answerType === 'TEXT') ? 0 : (parseInt(weightVal) || 0),
        
        explanation: explanationValue,
        targets: targets // 배열 통째로 전송
    };

    if (!data.typeId) { alert("평가 유형을 선택해주세요."); return; }
    if (!data.category) { alert("카테고리를 입력해주세요."); return; }
    if (!data.content) { alert("문항 내용을 입력해주세요."); return; }
    
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

// ==============================================
// 3. 새로운 트리 계층 구조 타겟 설정 함수들
// ==============================================

window.toggleTargetDisplay = function(isCommon) {
    const targetDiv = document.getElementById('targetSettingsDiv');
    if(targetDiv) {
        targetDiv.style.display = (isCommon === 'N') ? 'block' : 'none';
    }
};

window.loadDeptEmployees = function(deptId) {
    const empArea = document.getElementById('targetEmpArea');
    const checklist = document.getElementById('empChecklist');
    document.getElementById('selectAllEmp').checked = false;

    if (!deptId) {
        empArea.style.display = 'none';
        return;
    }

    fetch('/evaluation/employees/' + deptId)
        .then(res => res.json())
        .then(emps => {
            empArea.style.display = 'block';
            checklist.innerHTML = ''; 

            if (emps.length === 0) {
                checklist.innerHTML = '<span style="color:#9ca3af; font-size:13px; padding:5px;">소속된 사원이 없습니다.</span>';
                return;
            }

            emps.forEach(emp => {
                checklist.innerHTML += `
                    <label style="cursor: pointer; font-weight: normal; margin: 0; padding: 4px 8px; display: flex; align-items: center; gap: 8px;">
                        <input type="checkbox" class="emp-checkbox" value="${emp.empNo}" data-name="${emp.name}"> 
                        👤 ${emp.name} (${emp.empNo})
                    </label>
                `;
            });
        })
        .catch(err => {
            console.error('사원 로드 에러:', err);
            alert("사원 목록을 불러오는 중 오류가 발생했습니다.");
        });
};

window.toggleAllEmps = function(checkbox) {
    const empCheckboxes = document.querySelectorAll('.emp-checkbox');
    empCheckboxes.forEach(cb => {
        cb.checked = checkbox.checked;
        cb.disabled = checkbox.checked; 
    });
};

window.addTargetToList = function() {
    const deptSelect = document.getElementById('targetDeptSelect');
    const deptId = deptSelect.value;
    const deptName = deptSelect.options[deptSelect.selectedIndex].text;
    const isAll = document.getElementById('selectAllEmp').checked;
    const list = document.getElementById('finalTargetList');

    if (isAll) {
        if (!list.querySelector(`li[data-value="${deptId}"]`)) {
            list.innerHTML += `<li data-type="DEPT" data-value="${deptId}" style="display:flex; justify-content:space-between; align-items:center; padding:6px 10px; background:#eff6ff; margin-bottom:5px; border-radius:4px; font-size:13px;">
                <span>🏢 <b>${deptName}</b> (부서 전체 적용)</span>
                <button type="button" onclick="this.parentElement.remove()" style="border:none; background:none; color:#dc2626; cursor:pointer; font-size:16px;">&times;</button>
            </li>`;
        }
    } else {
        const checkedEmps = document.querySelectorAll('.emp-checkbox:checked');
        if (checkedEmps.length === 0) {
            alert('추가할 사원을 체크하거나, 부서 전체 적용을 체크해주세요.');
            return;
        }
        checkedEmps.forEach(cb => {
            if (!list.querySelector(`li[data-value="${cb.value}"]`)) {
                list.innerHTML += `<li data-type="EMP" data-value="${cb.value}" style="display:flex; justify-content:space-between; align-items:center; padding:6px 10px; background:#f3f4f6; margin-bottom:5px; border-radius:4px; font-size:13px;">
                    <span>👤 ${cb.getAttribute('data-name')} (${cb.value}) - <span style="color:#6b7280;">${deptName}</span></span>
                    <button type="button" onclick="this.parentElement.remove()" style="border:none; background:none; color:#dc2626; cursor:pointer; font-size:16px;">&times;</button>
                </li>`;
            }
        });
    }
    
    document.getElementById('selectAllEmp').checked = false;
    window.toggleAllEmps(document.getElementById('selectAllEmp'));
    document.querySelectorAll('.emp-checkbox').forEach(cb => cb.checked = false);
};