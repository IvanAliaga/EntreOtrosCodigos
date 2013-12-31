import serial
import time
from bottle import route, run, post, get, request, template

ser = serial.Serial('COM4', 9600)
time.sleep(2)

@route('/arduino', method='POST')
def arduino():
	led = request.forms.get('led')
	if(ser.isOpen()):
		ser.write(led)
	else:
		ser.open()
		ser.write(led)
		time.sleep(2)

run(host='192.168.2.10', port=8080)