# Dispatch Orchestrator

The purpose of the code is to dispatch (turn on) industrial assets and inform site owner's that their asset was either successfully or unsuccessfully dispatched. 
Integration with the asset and email sending are handled by third parties via http apis.

## Asset dispatch interface

`POST to /dispatch/<assetId>/<durationInSeconds>` 
where assetId is an int and durationInSeconds is how long to dispatch for in seconds
returns 200 OK if accepted, 400 otherwise

## Email notification interface

`POST to /email` with json body `{"contact" : {"email" : "<emailaddress>"}, "content" : "<textOfEmail>" }`
where emailaddress is email of contact and textOfEmail is email content
returns 200 OK if accepted, 400 otherwise

## Stories

As an aggregator 
I want to be able to dispatch assets
so i can participate in DSR programmes

As an aggregator
I want to know when an asset didn't dispatch successfully via a non-200 return code 
so I can manage my risk when participating in programmes

As an asset owner
I don't want my asset to be run for too short (less than 10 minutes) or too long (greater than 120 minutes)
So I keep my asset within warranty

As an asset owner 
I want to be emailed when my asset is dispatched
So I'm not surprised by my asset turning off/on

As an asset owner 
I want to be emailed when my asset fails to dispatch
So I can manually dispatch instead and still be paid




