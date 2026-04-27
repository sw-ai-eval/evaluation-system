package com.eval.domain.evaluation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eval.domain.evaluation.DeptEvalWeight;
import com.eval.domain.evaluation.repository.DeptEvalWeightRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    private final DeptEvalWeightRepository weightRepository;

    @Transactional
    public void saveDeptWeights(String deptId, List<DeptEvalWeight> weights) {
        int total = weights.stream().mapToInt(DeptEvalWeight::getWeight).sum();
        if (total != 100) {
            throw new IllegalArgumentException("가중치 합계는 반드시 100% 여야 합니다.");
        }

        weightRepository.saveAll(weights);
        
        
    }
}

