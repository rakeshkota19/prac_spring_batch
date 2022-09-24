# Demo project for playing with spring batch

    Used for Batch Processing Applications ( Schedulers, Crons) 
    Used for generating reports, mail campaigns, reminders..


> Inherently contains metadata repo for storing job data. This helps in error checking or restarting from a particular step ( like game checkpoint).
> Supports transactions support

    Job Name
    Job Parameters
    Job Instance
    Job Execution

    TaskLet - Contains a single execute function, it will run until it returns finished.
    Chunk 


### Job - Multiple Steps

    Job name + Job parameters -> Unique Job Instance  
    Once a job is completed we cannot re-execute it, only if it is paused or failed, it will re run from that step

    JobBuilder  

                start()
                .next().next()....build()

    StepBuilder

    Conditonal flow Transitioning

        if (status is this, do this this job else some other job)

        .next()
        .on("FAILED).to().
            .from().on("*).to()

        > from denotes else if
        > to denotes then
        > on is basically checking equality, * reperesents all states other than FAILED 


### Decider - Job Execution Decider

    We can also create sepearte classes for storing this deider logic
    
    public class DeciderClass implements JobExecutionDecider ( We can have custom status)
        We will override decide function, where we will place the decider logic.

        .next()
        .on("FAILED").to(decider())
            .on("FAILED").to()


###  Status

    Batch Status -> overall status of the job
    Exit Status -> status returned by the step function

    We use the exit status for conditional transfer
    Batch status is used for framework while analysing job, it decides whether to restart or not based on this status


###  Batch Status

    transtions
        end -> as this at end, even though any step is failed, Job batch status will be completed
        stop -> stops the job(pause), Batch status will be in Stopped State, and we can rereun it at later stage
        fail -> fails the job, and we can restart it

### Listeners
    Provides hooks into the flow of job and we can required logic at that time.
    
    JobExecutionListener, StepExecutionListener ... 

    StepExecutionListener
    Runs before and after the step, and we can pass the exit status accordingly ( same as decider)

### Reusablity of Jobs

    1. FLows
        We can create flows, containing same steps, allowing multiple jobs to use these flow
        We need to change only at one place to change step.

        We can reuse these flows in multiple jobs

        FlowBuilder<>().start().....;
        

    2. Nested Jobs
            A -> B -> C -> D
            C -> D ( one job Job-2)

            (Job 1) A -> B -> Job 2

            stepBuilderFactory.get().job().build();

        In Job 1, step C,D will run as a separate job

### Parallel Jobs
    Split(flows) -> allows us to run flows parallely
    Splits are used with flows, as opposed to jobs/steps

&nbsp;
### Reading Job Input

    Chunk based set
        Step -> ItemReader, Item Processor, Item writer
    chunk - contain some data ( n number)

    processing is done in chumk based.. ( process data one by one, after each chunk is processed, it will be written as a single chunk again)

    ItemReader
        Kafka
        FlatFile
        Jbdc
        Mongo
        Json.. 

        Chunk, arguments (Input type and output type)

        IteamReader, SimpleItemreader, FlatFileItemreader
        DefaultLineMapper, delimitedLineTokenizer

        JdbcCursor - for single threaded env
        JdbcPaging - for multi threaded env
    
        always try to sort database read data, as it helps to restart the job






    
        
    