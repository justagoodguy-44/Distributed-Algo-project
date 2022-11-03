#!/usr/bin/env python3

import os
import subprocess
import argparse
import time

OUT_FOLDER = "template_java/outputs"


def verify_correctness(nb_processes, nb_messages, delivering_process, crashed_processes):
  all_good = True
  for i in range(1, nb_processes+1):
    if i in crashed_processes: #Incorrect process
      continue
    
    f = open(f'{OUT_FOLDER}/proc{i}.output', 'r')
    if f:
      count = 0
      seq = 0
      l = f.readline()
      delivering = False
      if l:
        delivering = (l.split()[0] == 'd')
        if delivering and i != delivering_process:
          print(f"ERROR proc {i}: output file contains 'd': this process should NOT be the delivering process. Process {delivering_process} shoud be.")
          all_good = False
          f.close()
          continue
        if not delivering and i == delivering_process:
          print(f"ERROR proc {i}: output file doesn't contain 'b': this process should be the delivering process.")
          all_good = False
          f.close()
          continue
        if delivering:
          sets = [set() for i in range(nb_processes+1)]
      while l:
        count += 1
        line = l.split()
        if delivering:
          if len(line) != 3:
            print_error(i, delivering, "invalid deliver line format")
            all_good = False
            break
          if line[0] != 'd':
            print_error(i, delivering, "first character of the line should be 'd'")
            all_good = False
            break
          sender = int(line[1])
          if sender == i or sender > nb_processes or sender <= 0:
            print_error(i, delivering, f"invalid process id: {sender}")
            all_good = False
            break
          seq_nb = int(line[2])
          if seq_nb > nb_messages or seq_nb <= 0:
            print_error(i, delivering, f"invalid sequence number: {seq_nb}")
            all_good = False
            break
          if seq_nb in sets[sender]:
            print_error(i, delivering, f"duplication - sequence number {seq_nb} already delivered previously")
            all_good = False
            break
          sets[sender].add(seq_nb)
        else:
          if len(line) != 2:
            print_error(i, delivering, "invalid broadcast line format")
            all_good = False
            break
          if line[0] != 'b':
            print_error(i, delivering, "first character of the line should be 'b'")
            all_good = False
            break
          seq_nb = int(line[1])
          if seq_nb > nb_messages or seq_nb <= 0:
            print_error(i, delivering, f"invalid sequence number: {seq_nb}")
            all_good = False
            break
          if seq_nb <= seq:
            print_error(i, delivering, f"broadcasted sequence numbers should be in increasing order; received {seq_nb} but previous was {seq}")
            all_good = False
            break
          seq = seq_nb
        l = f.readline()
      if count == 0 and nb_messages > 0:
        print_error(i, delivering, "no logs written in output file")
        all_good = False
      if not delivering and count != nb_messages:
        print_error(i, delivering, f"the number of messages sent ({count}) differs from the number supposed to be sent ({nb_messages})")
        all_good = False
      if delivering:
        msg_sum = 0
        for i in [j for j in range(1, nb_processes+1) if j not in crashed_processes and j != delivering_process]:
          msg_sum += len(sets[i])
        if msg_sum != (nb_processes-1-len(crashed_processes))*nb_messages:
          print_error(i, delivering, f"the number of messages delivered from correct sending procecess ({msg_sum}) differs from the number supposed to be delivered ({(nb_processes-1-len(crashed_processes))*nb_messages})")
          all_good = False
      f.close()
    else:
      print_error(i, False, "could not open output file. Does it exist ?")
      all_good = False
  if all_good:
    print("CORRECT.")
  else:
    print("ERRORS...")
  return all_good



def print_error(process_nb, delivering, msg):
  if delivering:
    print(f"ERROR proc {process_nb} (delivering): ", msg)
  else:
    print(f"ERROR proc {process_nb}: ", msg)








def main(nb_processes, nb_messages):
  #If folder out_run/ does not exist, create it
  if(not os.path.isdir(OUT_FOLDER)):
    dir_path = os.path.dirname(os.path.realpath(__file__))
    path = os.path.join(dir_path, OUT_FOLDER)
    os.mkdir(path)

  #Build the project
  subprocess.call(["./template_java/build.sh"])

  #Run the project using nb_processes and nb_messages given as arguments
  p1 = subprocess.Popen(["python3", "tools/stress.py", "-r", "template_java/run.sh", "-t", "perfect", "-l", f"{OUT_FOLDER}/", "-p", f"{nb_processes}", "-m", f"{nb_messages}"], stdin=subprocess.PIPE, stdout=subprocess.PIPE)

  #Simulate pressing 'Enter' after X seconds
  X = 60
  time.sleep(X)
  out, err = p1.communicate(input=b'\n')
  p1.wait()
  crashed_processes = []
  lines = out.splitlines()
  for l in lines:
    ln = l.decode("utf-8")
    if "SIGTERM" in ln:
      words = ln.split()
      i = int(words[len(words)-1])
      print(f"Process {i} crashed (SIGTERM received)")
      crashed_processes.append(i)

  #Check if process that delivers has crashed (= is no longer correct)
  f = open(f'{OUT_FOLDER}/config', 'r')
  l = f.readline()
  if not l:
    print("ERROR with config file (it is empty), aborting test")
    return
  delivering_process = int(l.split()[1])
  f.close()
  if delivering_process in crashed_processes:
    print("Delivering process has crashed. Aborting test. Check the NO DUPLICATION and NO CREATION properties manually. For the NO CREATION property, make sure to log when sending, do not wait until receiving an ACK.")
    return
  if len(crashed_processes) == nb_processes-1:
    print("All sending processes have crashed, Aborting test. Check the NO DUPLICATION and NO CREATION properties manually. For the NO CREATION property, make sure to log when sending, do not wait until receiving an ACK.")
    return

  #Verify output files of each process and check for correctness
  #This assumes correct execution of all processes and delivery of all messages
  all_good = verify_correctness(nb_processes, nb_messages, delivering_process, crashed_processes)


  #Optionally clean the project using template_java/cleanup.sh
  subprocess.call(["./template_java/cleanup.sh"])

  #Optionally clean the out_run/ folder

if __name__ == "__main__":
  parser = argparse.ArgumentParser()

  parser.add_argument(
    "-p",
    "--processes",
    required=True,
    type=int,
    dest="nb_processes",
    help="Number of processes that broadcast"
  )

  parser.add_argument(
    "-m",
    "--messages",
    required=True,
    type=int,
    dest="nb_messages",
    help="Maximum number (because it can crash) of messages that each process can broadcast"
  )

  results = parser.parse_args()
  main(results.nb_processes, results.nb_messages)
