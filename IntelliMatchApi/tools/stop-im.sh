#!/bin/bash
sudo kill $(cat ./pid.file)
sudo rm ./pid.file
	