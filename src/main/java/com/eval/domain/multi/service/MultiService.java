/*
 * package com.eval.domain.multi.service;
 * 
 * import java.util.HashMap; import java.util.List; import java.util.Map;
 * 
 * import org.springframework.stereotype.Service;
 * 
 * import com.eval.domain.multi.dto.MultiEvalDTO; import
 * com.eval.domain.multi.mapper.MultiMapper;
 * 
 * import lombok.RequiredArgsConstructor;
 * 
 * @Service
 * 
 * @RequiredArgsConstructor public class MultiService{
 * 
 * private final MultiMapper multiMapper;
 * 
 * public List<MultiEvalDTO> getMultiList(String userNo, String position){
 * Map<String, Object> params = new HashMap<>(); params.put("userNo", userNo);
 * params.put("position", position); return multiMapper.findMultiEval(params); }
 * 
 * 
 * }
 */