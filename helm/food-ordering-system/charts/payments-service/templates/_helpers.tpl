{{- define "payments-service.fullname" -}}
{{ .Release.Name }}-payments-service
{{- end }}

{{- define "payments-service.labels" -}}
app: payments-service
app.kubernetes.io/name: payments-service
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "payments-service.selectorLabels" -}}
app: payments-service
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
