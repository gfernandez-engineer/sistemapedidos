{{- define "users-service.fullname" -}}
{{ .Release.Name }}-users-service
{{- end }}

{{- define "users-service.labels" -}}
app: users-service
app.kubernetes.io/name: users-service
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "users-service.selectorLabels" -}}
app: users-service
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
