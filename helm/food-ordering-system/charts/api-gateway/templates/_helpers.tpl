{{- define "api-gateway.fullname" -}}
{{ .Release.Name }}-api-gateway
{{- end }}

{{- define "api-gateway.labels" -}}
app: api-gateway
app.kubernetes.io/name: api-gateway
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "api-gateway.selectorLabels" -}}
app: api-gateway
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
