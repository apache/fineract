{{- define "secrets" -}}
fineract_tenants_pwd: {{ .Values.mysql.auth.rootPassword | b64enc }}
FINERACT_DEFAULT_TENANTDB_PWD: {{ .Values.mysql.auth.rootPassword | b64enc }}
{{ if .Values.extraSecretEnv }}
{{- range $key, $value := .Values.extraSecretEnv }}
{{ $key }}: {{ $value | b64enc }}
{{- end }}
{{- end }}
{{- end -}}

{{- define "mysql_host" -}}
{{ .Release.Name }}-mysql
{{- end -}}

{{- define "mysql_url" -}}
jdbc:mysql:thin://{{ include "mysql_host" . }}:3306/fineract_tenants
{{- end -}}

{{- define "mysql_user" -}}
{{ .Values.mysql.auth.username | default "root" }}
{{- end -}}
