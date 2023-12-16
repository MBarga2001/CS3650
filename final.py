import qiskit
from qiskit import *
import math

simulator = Aer.get_backend('qasm_simulator')
bits = ''
qr = qiskit.QuantumRegister(8)
cr = qiskit.ClassicalRegister(8)
circuit = qiskit.QuantumCircuit(qr, cr)
circuit.h(qr)
circuit.measure(qr, cr)

def set_token(token):
    global provider
    global qcomp
    IBMQ.save_account(token, overwrite = True)
    IBMQ.load_account()
    provider = IBMQ.get_provider('ibm-q')
    qcomp = provider.get_backend('ibm_osaka')


def bit_from_counts(counts):
    return [k for k, v in counts.items() if v == 1][0]


def request_bits(n):
    global bits
    iterations = math.ceil(n/circuit.width()*2)
    for _ in range(iterations):
        job = qiskit.execute(circuit, qcomp, shots=1)
        bits += bit_from_counts(job.result().get_counts())


def get_bit_string(n: int) -> str:
    global bits
    if len(bits) < n:
        request_bits(n-len(bits))
    bitString = bits[0:n]
    bits = bits[n:]
    return bitString


def get_random_int(min, max):
    delta = max - min
    n = math.floor(math.log(delta, 2)) + 1
    result = int(get_bit_string(n), 2)
    while(result > delta):
        result = int(get_bit_string(n), 2)
    result += min
    print(result)

# set_token()
# get_random_int(0, 100)