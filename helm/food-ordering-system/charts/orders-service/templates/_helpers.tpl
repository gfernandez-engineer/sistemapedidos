{{- define "orders-service.fullname" -}}
{{ .Release.Name }}-orders-service
{{- end }}

{{- define "orders-service.labels" -}}
app: orders-service
app.kubernetes.io/name: orders-service
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "orders-service.selectorLabels" -}}
app: orders-service
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
