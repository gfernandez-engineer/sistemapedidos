{{- define "deliveries-service.fullname" -}}
{{ .Release.Name }}-deliveries-service
{{- end }}

{{- define "deliveries-service.labels" -}}
app: deliveries-service
app.kubernetes.io/name: deliveries-service
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "deliveries-service.selectorLabels" -}}
app: deliveries-service
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
