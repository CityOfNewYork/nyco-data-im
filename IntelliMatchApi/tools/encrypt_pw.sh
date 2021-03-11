#===============================================================================
# This program is to encrypt your password for application.properties
#
# Usage : ./encrypt_pw.sh <password> <secretKey>
#
# Use output encrypted password replace password in application.properties
# 
# Notice that the encrypted password is wrapped in ENC(), this is important 
# to indicate that the value is encrypted, once seen, it is decrypted else 
# it is assumed to be plain. Sample,
#
# spring.datasource.password=ENC(<encrypted password>)
#
#===============================================================================
java -cp ./lib/jasypt-1.9.3.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input=%1 password=%2 algorithm=PBEWITHHMACSHA512ANDAES_256 ivGeneratorClassName=org.jasypt.iv.RandomIvGenerator
