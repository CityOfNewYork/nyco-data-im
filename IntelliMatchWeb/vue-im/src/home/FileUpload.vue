<template>
	<div>
		  <!--{{savePercent }}status:{{currentStatus}}-->
		  <div id="fileUpload" v-if="isAbleUpload">
			 <form enctype="multipart/form-data" novalidate v-if="isInitial || isSuccess || isFailed || savePercent === null || savePercent === 100">
			        <h2 class="mb-2">Data Source File</h2>
			        <p>Add files for your match. When all files are ready, you will be able to start your match.</p>
		
			        <div class="flex items-center justify-center w-full text-center" >
			          <div class="w-full p-12 border-dashed bg-gray-100 border border-gray-300 py-3 mb-3">
			          
			            <input type="file"  multiple :name="uploadFieldName" :disabled="isSaving"  @change="filesChange($event.target.name, $event.target.files); fileCount = $event.target.files.length"
			            :accept="acceptType" class="input-file">
		
			 			<p v-if="isInitial">
			              <div>Drag and drop a file here <br />or <span class="underline text-blue font-bold">Browse files</span></div>
			            </p>
		
			            <p v-if="isSaving">
			              Uploading files...
			            </p>
			            <p v-if="savedCount">
			              {{savedCount}} File uploaded successfully.
			            </p>

					    <!--FAILED-->
					    <div v-if="typeErrorCount">
					        <h4 class="text-red"> {{typeErrorCount}} {{typeErrorMessage}}</h4>
					    </div>
					    <div v-if="sizeErrorCount">
					        <h4 class="text-red"> {{sizeErrorCount}} File upload failed, size is over limit : {{sizeLimit | formatSize}} <br> contact us for Over size File Upload.</h4>
					    </div>
						<div v-if="uploadError">
					        <h4 class="text-red"> {{uploadError}} </h4>
					    </div>
			          </div>
			        </div>
			     </form>
		    </div>
			     
            <p v-if="isSaving">
				<progress-bar size="big" :val="savePercent" :text="savePercentTxt" :bar-border-radius="radius" bar-color="#2ECC71"></progress-bar>
            </p>
		    
  	</div>
</template>

<script>
  import { convertDict } from '../_helpers/utils';
  import { wait,uploadFilesizelimit,jobStatusCount,fileStatusCount } from '../_helpers/utils';
  import { userService } from '../_services';
  import ProgressBar from 'vue-simple-progress'
  
  const STATUS_INITIAL = 0, STATUS_SAVING = 1, STATUS_SUCCESS = 2, STATUS_FAILED = 3;

  export default {
    components: {
	    ProgressBar
	},
  
    name: 'fileUpload',
    data() {
      return {
      	totlCount:null,
      	savedCount:null,
      	savePercent:null,
      	savePercentTxt:'',

      	timer: '',
      	radius:5,

        typeErrorMsg:'',
	    typeErrorCount:0,
	    sizeErrorMsg:'',
	    sizeErrorCount:0,
        uploadError:'',
        
        currentStatus: null,
        uploadFieldName: 'file',

      }
    },
    computed: {
      dict () {
    	return convertDict(this.$store.state.dict.all.items);
      },
    
      isInitial() {
        return this.currentStatus === STATUS_INITIAL;
      },
      isSaving() {
        return this.currentStatus === STATUS_SAVING;
      },
      isSuccess() {
        return this.currentStatus === STATUS_SUCCESS;
      },
      isFailed() {
        return this.currentStatus === STATUS_FAILED;
      },

        files () {
            return this.$store.state.files.all;
        },
		fileStatusCount(){
			return fileStatusCount( this.files );
		},
		
	    jobs () {
	        return this.$store.state.jobs.all;
	    },
	    
		jobStatusCount(){
			return jobStatusCount( this.jobs );
		},

		isAbleUpload(){
        	// no job in pending/runing status
        	const jobCnt = this.jobStatusCount.pending + this.jobStatusCount.running;
        	const fileAnalyzingCnt = this.fileStatusCount.analyzing;
        	return (jobCnt === 0 && fileAnalyzingCnt === 0) ;
		},
		sizeLimit(){
			return uploadFilesizelimit();
		},
		serviceType(){
			return JSON.parse(localStorage.getItem('claims')).serviceType;
		},
		typeErrorMessage(){
			var msg = "";
			if(this.serviceType == 'AWS'){
				msg = this.dict.fileupload_item_aws;
				if(!msg){
					msg = 'File upload failed, AWS file upload supports csv(comm or tab delimiter) only.';
				}
			}else{
				msg = this.dict.fileupload_item_mdm;
				if(!msg){
					msg = 'File upload failed, MDM file upload supports dat(|| delimiter) only.';
				}
			}
			return msg;			
		},
		acceptType(){
			if(this.serviceType == 'AWS'){
				return '.csv,.txt';
			}else{
				return '.dat';
			}	
		}
		
		
    },

    methods: {
        reload: function(){
    		this.$store.dispatch('files/getAll');
        },
        reset() {
	        // reset form to initial state
	        clearInterval(this.timer);
	        this.currentStatus = STATUS_INITIAL;
	        
	        this.typeErrorMsg   = '';
	        this.typeErrorCount = 0;
	        this.sizeErrorMsg   = '';
	        this.sizeErrorCount = 0;
	        
	        this.savedCount    = null;
	        this.savePercent   = null;
	        this.reload();
        },
        
        save(formData) {
	        userService.upload(formData)
	          .then(wait(1500)) // DEV ONLY: wait for 1.5s
	          .then(x => {
					// uploaded successfully
		            this.savedCount+=1;
	
					// reset or reload
		            if( this.totlCount === this.savedCount){
		            	this.reset();
		            }else if ( (this.typeErrorCount + this.sizeErrorCount) === 0){
			            this.reload();
		            }
	          })
	          .catch(err => {
		     		// failed
		            this.uploadError += err.response;
	          });

        },
        filesChange(fieldName, fileList) {
        	// console.log('filesChange...');
        
	        // handle file changes
	        if (!fileList.length) return;
	
			// start progress bar
			this.savedCount=0;
			this.typeErrorCount=0;
			this.sizeErrorCount=0;
			this.savePercent=0;
			this.totlCount=fileList.length;
			this.currentStatus = STATUS_SAVING;
			this.timer = setInterval(this.startProgressBar, 1000);

	        // append the files to FormData
	        Array
	          .from(Array(fileList.length).keys())
	          .map(x => {
	          
	          	    // file validation check file type and size
		          	
		          	const isAwsFileType = /\.(csv|txt)$/i.test(fileList[x].name);
		          	const isMdmFileType = /\.(dat)$/i.test(fileList[x].name);
		          	const isFileType = (this.serviceType == 'AWS')?isAwsFileType:isMdmFileType;
		          	
		          	//const sizeLimit = uploadFilesizelimit();
		          	const isFileSize = (fileList[x].size <= this.sizeLimit);
		
					const formData = new FormData();
		            if (isFileType && isFileSize ) {
			            formData.append(fieldName, fileList[x], fileList[x].name);

			        	// save 1 by 1
			        	this.save(formData);
		        	}else if(!isFileType){
		        		// this.currentStatus = STATUS_FAILED;
				        this.typeErrorMsg   += fileList[x].name + '\n>';
				        this.typeErrorCount += 1;
		        	}else if(!isFileSize){
		        		// this.currentStatus = STATUS_FAILED;
				        this.sizeErrorMsg   += fileList[x].name + '\n';
				        this.sizeErrorCount += 1;
		        	}
	          });
	          
	          // update status if all looped
	          if( (this.totlCount - this.savedCount - this.typeErrorCount - this.sizeErrorCount) ===0){
	          		this.currentStatus = STATUS_INITIAL;
	          } 
	          
	      },
	      
	      startProgressBar() {
	      	 var donePer = (this.savedCount + this.typeErrorCount + this.sizeErrorCount) * 100 / this.totlCount;
	      	 
	      	 // inc
	      	 if(donePer < 100 && this.savePercent < 100){
	      	 	this.savePercent += 1;
	      	 }else{
	      	 	// waiting here
	      	 	this.savePercent = donePer;
	      	 }
	      	 this.savePercentTxt = 'Uploading and Scanning files : ' + this.savePercent + '%';
	      	 
			 // update status if all looped
	         if( (this.totlCount - this.savedCount - this.typeErrorCount - this.sizeErrorCount) ===0){
	          	 this.currentStatus = STATUS_INITIAL;
	         } 
	      },
	      

      },
      mounted() {
      		this.reset();
      },
  }

</script>

<style lang="scss">
  .input-file {
    opacity: 0; /* invisible but it's there! */
    width: 60%;
    height: 80px;
    position: absolute;
    cursor: pointer;
  }
</style>
