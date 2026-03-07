{{/*
Common labels
*/}}
{{- define "food-ordering.labels" -}}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: food-ordering-system
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{- end }}

{{/*
Namespace
*/}}
{{- define "food-ordering.namespace" -}}
{{ .Values.global.namespace | default "food-ordering" }}
{{- end }}
