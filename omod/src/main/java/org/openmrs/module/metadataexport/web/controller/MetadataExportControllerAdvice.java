/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.ValidationException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "org.openmrs.module.metadataexport.web.controller")
public class MetadataExportControllerAdvice {
	
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(ValidationException e) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("error", "Validation failed");
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		List<String> globalErrors = new ArrayList<>();
		if (e.getErrors() != null) {
			for (FieldError fieldError : e.getErrors().getFieldErrors()) {
				fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
			}
			for (ObjectError globalError : e.getErrors().getGlobalErrors()) {
				globalErrors.add(globalError.getDefaultMessage());
			}
		}
		body.put("fieldErrors", fieldErrors);
		body.put("globalErrors", globalErrors);
		return ResponseEntity.badRequest().body(body);
	}
	
	@ExceptionHandler({ APIAuthenticationException.class, ContextAuthenticationException.class })
	public ResponseEntity<Map<String, String>> handleAuthentication(APIException e) {
		HttpStatus status = Context.isAuthenticated() ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
		return ResponseEntity.status(status).body(Collections.singletonMap("error", e.getMessage()));
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
	}
	
	@ExceptionHandler(APIException.class)
	public ResponseEntity<Map<String, String>> handleApiException(APIException e) {
		log.warn("Metadata Export: service exception handling a REST request", e);
		return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) {
		log.error("Metadata Export: unexpected error handling a REST request", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		        .body(Collections.singletonMap("error", "An unexpected error occurred; see the server log"));
	}
}
