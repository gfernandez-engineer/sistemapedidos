{{- define "catalog-service.fullname" -}}
{{ .Release.Name }}-catalog-service
{{- end }}

{{- define "catalog-service.labels" -}}
app: catalog-service
app.kubernetes.io/name: catalog-service
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "catalog-service.selectorLabels" -}}
app: catalog-service
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
