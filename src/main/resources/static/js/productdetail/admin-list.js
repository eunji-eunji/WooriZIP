$(document).ready(function() {
    // 드롭다운 선택 처리
    $('#productSelect').on('change', function() {
        const selectedProductId = $(this).val();
        const selectedOption = $(this).find('option:selected');
        const hasDetail = selectedOption.data('has-detail');
        
        if (selectedProductId) {
            $('#goToDetailBtn').prop('disabled', false);
            
            const productName = selectedOption.text();
            const statusText = hasDetail ? '상세정보가 있습니다. 수정할 수 있습니다.' : '상세정보가 없습니다. 새로 등록할 수 있습니다.';
            
            $('#infoText').html(`<strong>${productName}</strong><br>${statusText}`);
            $('#selectedInfo').show();
        } else {
            $('#goToDetailBtn').prop('disabled', true);
            $('#selectedInfo').hide();
        }
    });

    // 관리하기 버튼 클릭
    $('#goToDetailBtn').on('click', function() {
        const selectedProductId = $('#productSelect').val();
        if (selectedProductId) {
            location.href = `/admin/product-details/${selectedProductId}/add`;
        }
    });

    // 필터 기능
    $('input[name="filter"]').on('change', function() {
        const filterValue = $(this).val();
        
        $('.product-item').each(function() {
            const hasDetail = $(this).data('has-detail');
            let shouldShow = false;
            
            switch(filterValue) {
                case 'all':
                    shouldShow = true;
                    break;
                case 'has':
                    shouldShow = hasDetail === true;
                    break;
                case 'none':
                    shouldShow = hasDetail === false;
                    break;
            }
            
            if (shouldShow) {
                $(this).show();
            } else {
                $(this).hide();
            }
        });
    });
    
    // 삭제 기능 추가
    $('.delete-detail-btn').on('click', function() {
        const productId = $(this).data('product-id');
        const productName = $(this).data('product-name');
        
        if (confirm(`"${productName}"의 상세정보를 정말 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`)) {
            // 삭제 요청 (CSRF 처리 단순화)
            $.ajax({
                url: `/admin/product-details/${productId}/delete`,
                type: 'POST',
                success: function(response) {
                    console.log('Response:', response);
                    if (response && response.success) {
                        alert(response.message || '상세정보가 삭제되었습니다.');
                        location.reload(); // 페이지 새로고침
                    } else {
                        alert(response.message || '삭제 중 오류가 발생했습니다.');
                    }
                },
                error: function(xhr, status, error) {
                    console.error('AJAX Error:', {xhr, status, error});
                    console.error('Response Text:', xhr.responseText);
                    alert('삭제 요청 중 오류가 발생했습니다.');
                }
            });
        }
    });
}); 