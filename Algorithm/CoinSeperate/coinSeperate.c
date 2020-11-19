#define _CRT_SECURE_NO_WARNINGS
#include <stdio.h>


void main()
{

	int haveCoin[4] = { 0,20,20,20 };
	int arrayDropCoin[4] = { 0, };

	int dropCoin = 0;

	int temp[2] = { 0, };
	
	int check = 0;

	printf("\n [남은 돈] 500원 - %d 개, 100원 - %d 개, 50원 - %d 개, 10원 - %d개\n", haveCoin[0], haveCoin[1], haveCoin[2], haveCoin[3]);
	printf("\n [계산금액 입력]: ");

	scanf("%d", &dropCoin);

	// 2500 , {2,20,20,20} => {2,20,5,0}

	temp[0] = dropCoin / 500; // 5
	temp[1] = dropCoin % 500;

	if (temp[0] > 0) {

		if (haveCoin[0] >= temp[0])
		{
			arrayDropCoin[0] = temp[0];
			haveCoin[0] = haveCoin[0] - temp[0];
			dropCoin = dropCoin - (arrayDropCoin[0] * 500); // 나머지 금액 정보를 변환후 temp[0]에 저장
			check = 1;
		}
		else if (haveCoin[0] < temp[0]) // 가지고 있는 금액보다 큰 경우는 
		{
			if (haveCoin[0] == 0) // 소유한 500원이 아예 없을 경우.
			{
				arrayDropCoin[0] = 0;
				check = 1;
			}
			else{ // 가지고 있는 500원의 금액은 부족하지만 0원은 아닌 경우

				arrayDropCoin[0] = haveCoin[0]; // 가지고있는 모든 500원을 temp[0]에 저장.
				haveCoin[0] = 0;

				dropCoin = dropCoin-(arrayDropCoin[0]*500); // 나머지 금액 정보를 변환후 temp[0]에 저장
				check = 1;
			}

		}

	}

	if (temp[0]==0 || check == 1) {

		temp[0] = dropCoin / 100;
		temp[1] = dropCoin % 100;
			
		if (haveCoin[1] >= temp[0]) 
		{
			arrayDropCoin[1] = temp[0];
			haveCoin[1] = haveCoin[1] - temp[0];
			dropCoin = dropCoin - (arrayDropCoin[1] * 100); // 나머지 금액 정보를 변환후 temp[0]에 저장
			check = 2;
		}
		else if (haveCoin[1] < temp[0]) // 가지고 있는 금액보다 큰 경우는 
		{

			if (haveCoin[1] == 0) // 소유한 100원이 아예 없을 경우.
			{
				arrayDropCoin[1] = 0;
				check = 2;
			}
			else { // 가지고 있는 100원의 금액은 부족하지만 0원은 아닌 경우

				arrayDropCoin[1] = haveCoin[1]; // 가지고있는 모든 500원을 temp[0]에 저장.
				haveCoin[0] = 0;

				dropCoin = dropCoin - (arrayDropCoin[1] * 100); // 나머지 금액 정보를 변환후 temp[0]에 저장
				check = 2;
			}
		}

	}

	if (temp[0] == 0 || check == 2 ){
		temp[0] = dropCoin / 50;
		temp[1] = dropCoin % 50;

		if (haveCoin[2] >= temp[0])
		{
			arrayDropCoin[2] = temp[0];
			haveCoin[2] = haveCoin[2] - temp[0];
			dropCoin = dropCoin - (arrayDropCoin[2] * 50); // 나머지 금액 정보를 변환후 temp[0]에 저장
			check = 3;
		}
		else if (haveCoin[2] < temp[0]) // 가지고 있는 금액보다 큰 경우는 
		{
			if (haveCoin[2] == 0) // 소유한 50원이 아예 없을 경우.
			{
				arrayDropCoin[2] = 0;
				check = 3;
			}
			else { // 가지고 있는 50원의 금액은 부족하지만 0원은 아닌 경우

				arrayDropCoin[2] = haveCoin[2]; // 가지고있는 모든 50을 temp[0]에 저장.
				haveCoin[2] = 0;

				dropCoin = dropCoin - (arrayDropCoin[2] * 50); // 나머지 금액 정보를 변환후 temp[0]에 저장
				check = 3;
			}
		}
	}

	if (temp[0] == 0 || check == 3)
	{
		temp[0] = dropCoin / 10;
		temp[1] = dropCoin % 10;

		if (haveCoin[3] >= temp[0])
		{
			arrayDropCoin[3] = temp[0];
			haveCoin[3] = haveCoin[3] - temp[0];
		
		}
		else if (haveCoin[3] < temp[0]) // 가지고 있는 금액보다 큰 경우는 
		{
			arrayDropCoin[3] = 0;
			check = 4;
			
		}
	}

	if ( check == 4)
	{
		printf("\n [** 배출할 동전이 부족합니다. ] \n");
	}
	else if (check != 4)
	{
		printf("\n [** 계산할 돈] 500원 - %d 개, 100원 - %d 개, 50원 - %d 개, 10원 - %d개\n", arrayDropCoin[0], arrayDropCoin[1], arrayDropCoin[2], arrayDropCoin[3]);
		printf("\n [** 남은 돈] 500원 - %d 개, 100원 - %d 개, 50원 - %d 개, 10원 - %d개\n", haveCoin[0], haveCoin[1], haveCoin[2], haveCoin[3]);

	
	}

}